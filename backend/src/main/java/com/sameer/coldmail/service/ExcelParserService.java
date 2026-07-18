package com.sameer.coldmail.service;

import com.sameer.coldmail.entity.Recruiter;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ExcelParserService {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("[\\w.+-]+@[\\w-]+\\.[\\w.-]+");

    // Parses an uploaded .xlsx/.xls file. Expects columns roughly like:
    // Name (optional) | Email
    // It's tolerant of column order - it scans each row for the first cell
    // that looks like an email address, and treats another text cell as the name.
    public List<Recruiter> parseExcel(MultipartFile file) throws IOException {
        List<Recruiter> recruiters = new ArrayList<>();

        try (InputStream is = file.getInputStream(); Workbook workbook = WorkbookFactory.create(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();

            for (Row row : sheet) {
                String name = null;
                String email = null;

                for (Cell cell : row) {
                    String value = formatter.formatCellValue(cell).trim();
                    if (value.isEmpty()) continue;

                    if (email == null && EMAIL_PATTERN.matcher(value).matches()) {
                        email = value;
                    } else if (name == null && !value.equalsIgnoreCase("name") && !value.equalsIgnoreCase("email")) {
                        name = value;
                    }
                }

                if (email != null) {
                    Recruiter r = new Recruiter();
                    r.setName(name); // can be null - that's fine
                    r.setEmail(email);
                    recruiters.add(r);
                }
            }
        }
        return recruiters;
    }

    // Parses pasted free text - one recruiter per line.
    // Supported line formats:
    //   "John Doe, john@company.com"
    //   "John Doe - john@company.com"
    //   "john@company.com"                (name-less)
    public List<Recruiter> parseText(String text) {
        List<Recruiter> recruiters = new ArrayList<>();
        if (text == null || text.isBlank()) return recruiters;

        String[] lines = text.split("\\r?\\n");
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;

            Matcher m = EMAIL_PATTERN.matcher(line);
            if (!m.find()) continue;

            String email = m.group();
            String rest = line.replace(email, "")
                    .replaceAll("[,\\-|]+", " ")
                    .trim();

            Recruiter r = new Recruiter();
            r.setEmail(email);
            r.setName(rest.isEmpty() ? null : rest);
            recruiters.add(r);
        }
        return recruiters;
    }
}
