package com.visbliss.salarycalculatorservice.service.impl;

import com.ibm.icu.text.NumberFormat;
import com.visbliss.salarycalculatorservice.model.TaxRegime;
import com.visbliss.salarycalculatorservice.service.SalaryCalculator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Format;
import java.util.Locale;

@Service
@Slf4j
public class SalaryCalculatorImpl implements SalaryCalculator {

    private static final long HOUSE_RENT = 24_200L;
    private static final Format CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(new Locale("en", "in"));

    @Override
    public String calculateTakeHomeSalary(double gross, boolean isPayingHomeLoan) {
        BigDecimal grossSalary = BigDecimal.valueOf(gross);
        BigDecimal basicPay = grossSalary.divide(BigDecimal.valueOf(2));
        BigDecimal epf = calculateEPF(basicPay);
        BigDecimal gratuity = calculateGratuity(basicPay);
        log.info("Fixed Pay: {}", CURRENCY_FORMAT.format(
                grossSalary.add(epf).add(gratuity).setScale(2, RoundingMode.HALF_UP)));
        BigDecimal taxableIncome = grossSalary.subtract(calculateHRA(BigDecimal.valueOf(HOUSE_RENT), basicPay)).subtract(getProfessionalTax()).subtract(getStandardDeduction()).subtract(isPayingHomeLoan ? BigDecimal.valueOf(4_00_000) : BigDecimal.valueOf(2_00_000));
        BigDecimal incomeTax = calculateIncomeTax(taxableIncome, TaxRegime.OLD);
        BigDecimal netPay = grossSalary.subtract(incomeTax).subtract(epf).subtract(getProfessionalTax());
        BigDecimal takeHomeSalary = netPay.divide(BigDecimal.valueOf(12), 0, RoundingMode.HALF_UP).subtract(BigDecimal.valueOf(50));
        return CURRENCY_FORMAT.format(takeHomeSalary);
    }

    private BigDecimal calculateHRA(BigDecimal monthlyRentPaid, BigDecimal basicPay) {
        BigDecimal hra = monthlyRentPaid.multiply(BigDecimal.valueOf(12)).subtract(BigDecimal.valueOf(0.1).multiply(basicPay));
        BigDecimal halfOfBasicPay = basicPay.divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
        BigDecimal returnValue = hra.compareTo(halfOfBasicPay) <= 0 ? hra : halfOfBasicPay;
        return returnValue;
    }

    private BigDecimal calculateIncomeTax(BigDecimal taxableIncome, TaxRegime taxRegime) {
        BigDecimal taxUpto10Lakhs = BigDecimal.valueOf(1_12_500);
        BigDecimal incomeOver10Lakhs = taxableIncome.subtract(BigDecimal.valueOf(10_00_000));
        BigDecimal taxOver10Lakhs = incomeOver10Lakhs.multiply(BigDecimal.valueOf(0.3));
        BigDecimal taxWithOutCess = taxUpto10Lakhs.add(taxOver10Lakhs);
        BigDecimal returnValue = taxWithOutCess.add(calculateEducationCess(taxWithOutCess));
        return returnValue;
    }

    private BigDecimal getProfessionalTax() {
        return BigDecimal.valueOf(2_500);
    }

    private BigDecimal getStandardDeduction() {
        return BigDecimal.valueOf(50_000);
    }

    private BigDecimal calculateEPF(BigDecimal basicPay) {
        BigDecimal epf = basicPay.multiply(BigDecimal.valueOf(0.12));
        return epf;
    }

    private BigDecimal calculateEducationCess(BigDecimal incomeTax) {
        return incomeTax.multiply(BigDecimal.valueOf(0.04));
    }

    private BigDecimal calculateGratuity(BigDecimal basicPay) {
        BigDecimal gratuity = basicPay.multiply(BigDecimal.valueOf(0.0481));
        return gratuity;
    }
}
