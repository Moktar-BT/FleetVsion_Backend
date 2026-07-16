package com.Transami.Transami.service;

import com.Transami.Transami.dao.InvoiceNumberCounterDao;
import com.Transami.Transami.entity.InvoiceNumberCounter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class InvoiceNumberService {

    private final InvoiceNumberCounterDao counterDao;

    @Transactional
    public synchronized String generateNextNumber(LocalDate invoiceDate, Long adminId) {
        int year = invoiceDate.getYear();
        InvoiceNumberCounter counter = counterDao.findByYearAndAdminIdWithLock(year, adminId)
                .orElse(new InvoiceNumberCounter(null, year, adminId, 0L));

        long nextNumber = counter.getCurrentNumber() + 1;
        counter.setCurrentNumber(nextNumber);
        counterDao.save(counter);

        return nextNumber + "/" + year;
    }

    @Transactional
    public synchronized void decrementIfLastNumber(String numeroFacture, Long adminId) {
        // Parse le numéro de facture (format: "123/2026")
        String[] parts = numeroFacture.split("/");
        if (parts.length != 2) {
            return; // Format invalide, on ne fait rien
        }

        try {
            long number = Long.parseLong(parts[0]);
            int year = Integer.parseInt(parts[1]);

            InvoiceNumberCounter counter = counterDao.findByYearAndAdminIdWithLock(year, adminId)
                    .orElse(null);

            // Décrémenter seulement si c'est le dernier numéro généré
            if (counter != null && counter.getCurrentNumber() == number) {
                counter.setCurrentNumber(number - 1);
                counterDao.save(counter);
            }
        } catch (NumberFormatException e) {
            // Format invalide, on ne fait rien
        }
    }
}