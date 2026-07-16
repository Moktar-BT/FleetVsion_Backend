package com.Transami.Transami.service;

import lombok.RequiredArgsConstructor;
import com.Transami.Transami.dao.CodeProduitDao;
import com.Transami.Transami.dto.CodeProduitRequest;
import com.Transami.Transami.dto.CodeProduitResponse;
import com.Transami.Transami.entity.CodeProduit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CodeProduitService {

    private final CodeProduitDao codeProduitDao;

    @Transactional
    public CodeProduitResponse create(CodeProduitRequest request, Long adminId) {
        if (codeProduitDao.existsByCodeAndAdminId(request.getCode(), adminId)) {
            throw new RuntimeException("Un code produit avec ce code existe déjà : " + request.getCode());
        }

        CodeProduit codeProduit = CodeProduit.builder()
                .code(request.getCode())
                .adminId(adminId)
                .description(request.getDescription())
                .unitPrice(request.getUnitPrice())
                .unit(request.getUnit())
                .vat(request.getVat())
                .build();

        return toResponse(codeProduitDao.save(codeProduit));
    }

    public CodeProduitResponse getById(Long id, Long adminId) {
        CodeProduit cp = codeProduitDao.findByIdAndAdminId(id, adminId)
                .orElseThrow(() -> new RuntimeException("Code produit non trouvé ou accès refusé"));
        return toResponse(cp);
    }

    public List<CodeProduitResponse> getAll(Long adminId) {
        return codeProduitDao.findAllByAdminId(adminId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<CodeProduitResponse> getAll() {
        return codeProduitDao.findAll()
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public CodeProduitResponse update(Long id, CodeProduitRequest request, Long adminId) {
        CodeProduit cp = codeProduitDao.findByIdAndAdminId(id, adminId)
                .orElseThrow(() -> new RuntimeException("Code produit non trouvé ou accès refusé"));

        if (!cp.getCode().equals(request.getCode()) && codeProduitDao.existsByCodeAndAdminId(request.getCode(), adminId)) {
            throw new RuntimeException("Un code produit avec ce code existe déjà");
        }

        cp.setCode(request.getCode());
        cp.setDescription(request.getDescription());
        cp.setUnitPrice(request.getUnitPrice());
        cp.setUnit(request.getUnit());
        cp.setVat(request.getVat());

        return toResponse(codeProduitDao.save(cp));
    }

    @Transactional
    public void delete(Long id, Long adminId) {
        CodeProduit cp = codeProduitDao.findByIdAndAdminId(id, adminId)
                .orElseThrow(() -> new RuntimeException("Code produit non trouvé ou accès refusé"));
        codeProduitDao.delete(cp);
    }

    private CodeProduitResponse toResponse(CodeProduit cp) {
        return CodeProduitResponse.builder()
                .id(cp.getId())
                .code(cp.getCode())
                .description(cp.getDescription())
                .unitPrice(cp.getUnitPrice())
                .unit(cp.getUnit())
                .vat(cp.getVat())
                .build();
    }
}