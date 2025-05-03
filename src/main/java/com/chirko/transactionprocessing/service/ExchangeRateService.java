package com.chirko.transactionprocessing.service;

import com.chirko.transactionprocessing.dto.ForexData;
import com.chirko.transactionprocessing.exception.ErrorCode;
import com.chirko.transactionprocessing.exception.TransactionProcessingException;
import com.chirko.transactionprocessing.model.cassandra.ExchangeRate;
import com.chirko.transactionprocessing.model.cassandra.ExchangeRateId;
import com.chirko.transactionprocessing.model.emuns.CurrencyShortname;
import com.chirko.transactionprocessing.repository.cassandra.ExchangeRateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static com.chirko.transactionprocessing.model.emuns.CurrencyShortname.*;

@Service
public class ExchangeRateService {

    private final Logger logger = LoggerFactory.getLogger(ExchangeRateService.class);

    private static volatile boolean isFetchedAfterStartup = false;

    private final String apiKey;

    private final String function = "FX_DAILY";

    private final WebClient webClient;

    private final ExchangeRateRepository exchangeRateRepository;

    public ExchangeRateService(
            @Value("${exchange.rate.service.provider.base-url}") String baseUrl,
            @Value("${exchange.rate.service.provider.api-key}") String apiKey,
            ExchangeRateRepository exchangeRateRepository) {
        this.apiKey = apiKey;
        this.webClient = WebClient.builder().baseUrl(baseUrl).build();
        this.exchangeRateRepository = exchangeRateRepository;
    }

    public BigDecimal getExchangeRate(CurrencyShortname base, CurrencyShortname target)
            throws TransactionProcessingException {
        if (base.equals(target)) {
            return BigDecimal.ONE;
        }

        final LocalDate now = LocalDate.now();

        final LocalDate date = switch (now.getDayOfWeek()) {
            case SATURDAY -> now.minusDays(1);
            case SUNDAY -> now.minusDays(2);
            default -> LocalTime.now().isBefore(LocalTime.of(13, 10))
                    ? now.minusDays(1)
                    : now;
        };

        final ExchangeRateId id = ExchangeRateId.builder()
                .base(base)
                .target(target)
                .date(date)
                .build();

        if (!isFetchedAfterStartup) {
            synchronized (ExchangeRateService.class) {
                if (!isFetchedAfterStartup) {
                    fetchLatestExchangeRateData();
                }
            }
        }

        return exchangeRateRepository.findById(id)
                .orElseThrow(() -> new TransactionProcessingException(ErrorCode.INTERNAL_SERVICE_ERROR,
                        String.format("No Cached exchange rate data for base: %s, target: %s; on: %s",
                                base, target, date)))
                .rate();
    }

    // At 13:10 every day between Monday and Friday inclusive
    @Scheduled(cron = "0 10 13 * * MON-FRI", zone = "${scheduler.service.zone}")
    private void fetchLatestExchangeRateData() {
        logger.info("Fetching latest exchange rate data");

        final List<ExchangeRate> rates = Stream.of(getRates(KZT, USD), getRates(RUB, USD))
                .flatMap(Collection::stream)
                .toList();

        exchangeRateRepository.saveAll(rates);

        if (!isFetchedAfterStartup) {
            synchronized (ExchangeRateService.class) {
                if (!isFetchedAfterStartup) {
                    isFetchedAfterStartup = true;
                }
            }
        }
    }

    private List<ExchangeRate> getRates(CurrencyShortname base, CurrencyShortname target) {
        logger.info("Processing GET request with param: base currency: {}, target currency: {}", base, target);

        final ForexData forexData = Optional.ofNullable(webClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .queryParam("function", function)
                                .queryParam("from_symbol", base)
                                .queryParam("to_symbol", target)
                                .queryParam("apikey", apiKey)
                                .build())
                        .retrieve()
                        .bodyToMono(ForexData.class)
                        .block())
                .orElseThrow(() -> {
                    logger.error("No exchange rate data available for base: {}, target: {}", base, target);
                    return new RuntimeException(
                            String.format("No exchange rate data available for base: %s, target: %s", base, target));
                });

        return forexData.timeSeries().entrySet().stream()
                .map(dateDailyDataEntry -> ExchangeRate.builder()
                        .exchangeRateId(ExchangeRateId.builder()
                                .base(base)
                                .target(target)
                                .date(dateDailyDataEntry.getKey())
                                .build())
                        .rate(dateDailyDataEntry.getValue().close())
                        .build())
                .toList();
    }
}
