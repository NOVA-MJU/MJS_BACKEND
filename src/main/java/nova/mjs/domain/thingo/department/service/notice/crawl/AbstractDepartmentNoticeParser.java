package nova.mjs.domain.thingo.department.service.notice.crawl;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

abstract class AbstractDepartmentNoticeParser implements DepartmentNoticeListParser {

    protected List<CrawledDepartmentNotice> parseRows(
            Elements rows,
            DepartmentNoticeSource source,
            String titleSelector,
            String linkSelector,
            String dateSelector
    ) {
        List<CrawledDepartmentNotice> notices = new ArrayList<>();

        for (Element row : rows) {
            String title = row.select(titleSelector).text().trim();
            String link = row.select(linkSelector).attr("href").trim();
            String dateText = row.select(dateSelector).text().trim();

            if (title.isBlank() || link.isBlank()) {
                continue;
            }

            LocalDateTime date = parseDate(dateText);
            String absoluteLink = toAbsoluteUrl(source.url(), row.select(linkSelector).attr("abs:href"), link);

            if (date == null || absoluteLink.isBlank()) {
                continue;
            }

            notices.add(new CrawledDepartmentNotice(title, date, absoluteLink));
        }

        return notices;
    }

    protected LocalDateTime parseDate(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }

        String normalized = raw
                .replace('.', '-')
                .replace('/', '-')
                .replace("년", "-")
                .replace("월", "-")
                .replace("일", "")
                .trim();

        List<DateTimeFormatter> dateTimeFormats = List.of(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
                DateTimeFormatter.ofPattern("yyyy-M-d HH:mm")
        );

        for (DateTimeFormatter formatter : dateTimeFormats) {
            try {
                return LocalDateTime.parse(normalized, formatter);
            } catch (DateTimeParseException ignored) {
            }
        }

        List<DateTimeFormatter> dateFormats = List.of(
                DateTimeFormatter.ofPattern("yyyy-MM-dd"),
                DateTimeFormatter.ofPattern("yyyy-M-d"),
                DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH)
        );

        for (DateTimeFormatter formatter : dateFormats) {
            try {
                return LocalDate.parse(normalized, formatter).atTime(LocalTime.NOON);
            } catch (DateTimeParseException ignored) {
            }
        }

        return null;
    }

    protected String toAbsoluteUrl(String baseUrl, String absHref, String href) {
        if (absHref != null && !absHref.isBlank()) {
            return absHref;
        }

        if (href == null || href.isBlank()) {
            return "";
        }

        try {
            return URI.create(baseUrl).resolve(href).toString();
        } catch (Exception e) {
            return href;
        }
    }
}
