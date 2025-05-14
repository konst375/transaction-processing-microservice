package com.chirko.transactionprocessing.service;

import com.chirko.transactionprocessing.dto.ForexData;
import com.chirko.transactionprocessing.exception.ErrorCode;
import com.chirko.transactionprocessing.exception.TransactionProcessingException;
import com.chirko.transactionprocessing.model.cassandra.ExchangeRate;
import com.chirko.transactionprocessing.model.cassandra.ExchangeRateId;
import com.chirko.transactionprocessing.model.emuns.CurrencyShortname;
import com.chirko.transactionprocessing.model.postgres.ExchangeRateServiceMetadata;
import com.chirko.transactionprocessing.repository.cassandra.ExchangeRateRepository;
import com.chirko.transactionprocessing.repository.postgres.ExchangeRateServiceMetadataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static com.chirko.transactionprocessing.model.emuns.CurrencyShortname.*;

@Service
public class ExchangeRateService {

    private final Logger logger = LoggerFactory.getLogger(ExchangeRateService.class);

    private final String apiKey;

    private final String function = "FX_DAILY";

    // 100 last days for compact and 200 for full
    private final String outputsize = "compact"; // or full

    private final WebClient webClient;

    private final ExchangeRateRepository exchangeRateRepository;

    private final ExchangeRateServiceMetadataRepository exchangeRateServiceMetadataRepository;

    public ExchangeRateService(
            @Value("${exchange.rate.service.provider.base-url}") String baseUrl,
            @Value("${exchange.rate.service.provider.api-key}") String apiKey,
            ExchangeRateRepository exchangeRateRepository,
            ExchangeRateServiceMetadataRepository exchangeRateServiceMetadataRepository) {
        this.apiKey = apiKey;
        this.webClient = WebClient.builder().baseUrl(baseUrl).build();
        this.exchangeRateRepository = exchangeRateRepository;
        this.exchangeRateServiceMetadataRepository = exchangeRateServiceMetadataRepository;
    }

    @EventListener(ContextRefreshedEvent.class)
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    void doOnStartup() {
        final LocalDate date = getDate();
        if (exchangeRateServiceMetadataRepository.findById(date).isEmpty()) {
            fetchLatestExchangeRateData();
        }
    }

    public BigDecimal getExchangeRate(CurrencyShortname base, CurrencyShortname target)
            throws TransactionProcessingException {
        if (base.equals(target)) {
            return BigDecimal.ONE;
        }

        final LocalDate date = getDate();

        final ExchangeRateId id = ExchangeRateId.builder()
                .base(base)
                .target(target)
                .date(date)
                .build();

        return exchangeRateRepository.findById(id)
                // That throwing can be omitted if you use API that can return rate for certain date like
                // https://api.freecurrencyapi.com/v1/historical
                // API docs:
                // https://freecurrencyapi.com/docs/historical#historical-exchange-rates
                .orElseThrow(() -> new TransactionProcessingException(ErrorCode.INTERNAL_SERVICE_ERROR,
                        String.format("No Cached exchange rate data for base: %s, target: %s; on: %s",
                                base, target, date)))
                .rate();
    }

    // At 13:10 every day between Monday and Friday inclusive
    // And on startup if fetching is needed today at current time
    @Scheduled(cron = "0 10 13 * * MON-FRI", zone = "${scheduler.service.zone}")
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    protected void fetchLatestExchangeRateData() {
        logger.info("Fetching latest exchange rate data");

        final double blockingFactor = 0.5;
        final int availableProcessors = Runtime.getRuntime().availableProcessors();
        final int maximumPoolSize = (int) (availableProcessors / (1 - blockingFactor));

        List<Runnable> tasks = List.of(() -> fetchRates(KZT, USD), () -> fetchRates(RUB, USD));

        Executor executor = Executors.newFixedThreadPool(Math.max(maximumPoolSize, tasks.size()));
        tasks.forEach(executor::execute);

        exchangeRateServiceMetadataRepository.save(new ExchangeRateServiceMetadata(getDate()));
    }

    private LocalDate getDate() {
        final LocalDate now = LocalDate.now();
        return switch (now.getDayOfWeek()) {
            case SATURDAY -> now.minusDays(1);
            case SUNDAY -> now.minusDays(2);
            default -> LocalTime.now().isBefore(LocalTime.of(13, 10))
                    ? now.minusDays(1)
                    : now;
        };
    }

    private void fetchRates(CurrencyShortname base, CurrencyShortname target) {
        logger.info("Processing GET request with param: base currency: {}, target currency: {}", base, target);

        final ForexData forexData = Optional.ofNullable(webClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .queryParam("function", function)
                                .queryParam("from_symbol", base)
                                .queryParam("to_symbol", target)
                                .queryParam("outputsize", outputsize)
                                .queryParam("apikey", apiKey)
                                .build())
                        .retrieve()
                        .bodyToMono(ForexData.class)
                        .block())
                .orElseThrow(() -> {
                    final String message =
                            String.format("No exchange rate data available for base: %s, target: %s", base, target);
                    logger.error(message);
                    return new RuntimeException(message); // throws RuntimeException to provoke transaction rollback
                });

        final List<ExchangeRate> rates = forexData.timeSeries().entrySet().stream()
                .map(dateDailyDataEntry -> ExchangeRate.builder()
                        .exchangeRateId(ExchangeRateId.builder()
                                .base(base)
                                .target(target)
                                .date(dateDailyDataEntry.getKey())
                                .build())
                        .rate(dateDailyDataEntry.getValue().close())
                        .build())
                .filter(rate -> !exchangeRateRepository.existsById(rate.exchangeRateId()))
                .toList();

        exchangeRateRepository.saveAll(rates);
    }
}
