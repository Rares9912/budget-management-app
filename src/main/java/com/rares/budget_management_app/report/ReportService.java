package com.rares.budget_management_app.report;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.rares.budget_management_app.budget.Budget;
import com.rares.budget_management_app.budget.BudgetRepository;
import com.rares.budget_management_app.category.Category;
import com.rares.budget_management_app.category.CategoryRepository;
import com.rares.budget_management_app.expense.Expense;
import com.rares.budget_management_app.expense.ExpenseRepository;
import com.rares.budget_management_app.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.Month;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReportService {

    private static final Color HEADER_BG  = new Color(48, 63, 159);
    private static final Color COLOR_GREEN = new Color(27, 94, 32);
    private static final Color COLOR_RED   = new Color(183, 28, 28);

    private final CategoryRepository categoryRepository;
    private final BudgetRepository budgetRepository;
    private final ExpenseRepository expenseRepository;

    public byte[] generateMonthlyReport(User user, int month, int year) {
        List<Category> categories = categoryRepository.findAllByUserId(user.getId());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 40, 40, 60, 40);

        try {
            PdfWriter.getInstance(document, baos);
            document.open();

            addTitle(document, "Monthly summary");
            addSubtitle(document, Month.of(month).name() + " " + year);
            addUserInfo(document, user);
            document.add(Chunk.NEWLINE);

            PdfPTable table = createTable(new float[]{3, 2, 2, 2});
            addTableHeader(table, "Category", "Budget", "Expenses", "Difference");

            for (Category category : categories) {
                Optional<Budget> budgetOpt = budgetRepository.findByUserIdAndCategoryIdAndMonthAndYear(
                        user.getId(), category.getId(), month, year);

                BigDecimal spent = sumExpenses(expenseRepository.findAllByUserIdAndCategoryIdAndMonthAndYear(
                        user.getId(), category.getId(), month, year));

                BigDecimal budgetValue = budgetOpt.map(Budget::getValue).orElse(null);

                addDataCell(table, category.getName());
                addDataCell(table, budgetValue != null ? format(budgetValue) : "-");
                addDataCell(table, format(spent));

                if (budgetValue != null) {
                    BigDecimal diff = budgetValue.subtract(spent);
                    addColoredCell(table, format(diff), diff.compareTo(BigDecimal.ZERO) >= 0 ? COLOR_GREEN : COLOR_RED);
                } else {
                    addDataCell(table, "-");
                }

            }

            document.add(table);
            document.close();

        } catch (DocumentException e) {
            throw new RuntimeException("Failed to generate monthly PDF report", e);
        }

        return baos.toByteArray();
    }

    public byte[] generateAnnualReport(User user, int year) {
        List<Category> categories = categoryRepository.findAllByUserId(user.getId());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 40, 40, 60, 40);

        try {
            PdfWriter.getInstance(document, baos);
            document.open();

            addTitle(document, "Annual Summary");
            addSubtitle(document, String.valueOf(year));
            addUserInfo(document, user);
            document.add(Chunk.NEWLINE);

            PdfPTable table = createTable(new float[]{3, 2, 2, 2});
            addTableHeader(table, "Category", "Budget", "Expenses", "Difference");

            for (int m = 1; m <= 12; m++) {
                addMonthHeaderRow(table, Month.of(m).name() + " " + year);

                for (Category category : categories) {
                    Optional<Budget> budgetOpt = budgetRepository.findByUserIdAndCategoryIdAndMonthAndYear(
                            user.getId(), category.getId(), m, year);

                    BigDecimal spent = sumExpenses(expenseRepository.findAllByUserIdAndCategoryIdAndMonthAndYear(
                            user.getId(), category.getId(), m, year));

                    BigDecimal budgetValue = budgetOpt.map(Budget::getValue).orElse(null);

                    addDataCell(table, category.getName());
                    addDataCell(table, budgetValue != null ? format(budgetValue) : "-");
                    addDataCell(table, format(spent));

                    if (budgetValue != null) {
                        BigDecimal diff = budgetValue.subtract(spent);
                        addColoredCell(table, format(diff), diff.compareTo(BigDecimal.ZERO) >= 0 ? COLOR_GREEN : COLOR_RED);
                    } else {
                        addDataCell(table, "-");
                    }

                }

            }

            document.add(table);
            document.close();

        } catch (DocumentException e) {
            throw new RuntimeException("Failed to generate annual PDF report", e);
        }

        return baos.toByteArray();
    }

    private void addTitle(Document doc, String text) throws DocumentException {
        Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, Color.BLACK);
        Paragraph p = new Paragraph(text, font);
        p.setAlignment(Element.ALIGN_CENTER);
        p.setSpacingAfter(4);
        doc.add(p);
    }

    private void addSubtitle(Document doc, String text) throws DocumentException {
        Font font = FontFactory.getFont(FontFactory.HELVETICA, 14, Color.DARK_GRAY);
        Paragraph p = new Paragraph(text, font);
        p.setAlignment(Element.ALIGN_CENTER);
        p.setSpacingAfter(12);
        doc.add(p);
    }

    private void addUserInfo(Document doc, User user) throws DocumentException {
        Font font = FontFactory.getFont(FontFactory.HELVETICA, 11, Color.BLACK);
        doc.add(new Paragraph("User: " + user.getName(), font));
        doc.add(new Paragraph("Email: " + user.getEmail(), font));
    }

    private PdfPTable createTable(float[] widths) throws DocumentException {
        PdfPTable table = new PdfPTable(widths.length);
        table.setWidthPercentage(100);
        table.setWidths(widths);
        table.setSpacingBefore(8);
        return table;
    }

    private void addTableHeader(PdfPTable table, String... headers) {
        Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, Color.WHITE);
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, font));
            cell.setBackgroundColor(HEADER_BG);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(8);
            table.addCell(cell);
        }
    }

    private void addDataCell(PdfPTable table, String text) {
        Font font = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(6);
        table.addCell(cell);

    }

    private void addColoredCell(PdfPTable table, String text, Color color) {
        Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, color);
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(6);
        table.addCell(cell);
    }

    private void addMonthHeaderRow(PdfPTable table, String monthLabel) {
        Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, Color.WHITE);
        PdfPCell cell = new PdfPCell(new Phrase(monthLabel, font));
        cell.setColspan(4);
        cell.setBackgroundColor(HEADER_BG);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setPadding(7);
        table.addCell(cell);
    }

    private BigDecimal sumExpenses(List<Expense> expenses) {
        return expenses.stream()
                .map(e -> e.getValue().multiply(e.getExchangeRate()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String format(BigDecimal value) {
        return String.format("%.2f", value);
    }
}