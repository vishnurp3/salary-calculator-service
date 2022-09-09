package com.visbliss.salarycalculatorservice.service.impl;

import com.ibm.icu.text.NumberFormat;
import com.visbliss.salarycalculatorservice.service.SalaryCalculator;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.text.Format;
import java.util.Locale;

@Slf4j
public class SalaryCalculatorTest {

    private SalaryCalculator salaryCalculator = new SalaryCalculatorImpl();
    private static final Format CURRENCY_FORMAT
            = NumberFormat.getCurrencyInstance(new Locale("en", "in"));

    @Test
    public void should_return_take_home_salary_given_gross_pay_for_10_years() {
        double gross = 0;
        for (int i = 0; i < 10; i++) {
            String salary = salaryCalculator.calculateTakeHomeSalary(gross, true);
            log.info("Take Home Salary: {}\n******************************", salary);
            gross = gross * 1.1;
        }
    }

    @Test
    public void should_return_take_home_salary_given_gross_pay() {
        double gross = 0;
        String sb = String.format("Take Home Salary: %s"
                , salaryCalculator.calculateTakeHomeSalary(gross, true));
        System.out.println(sb);
    }
}
