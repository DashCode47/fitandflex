package com.backoffice.fitandflex.service;

import com.backoffice.fitandflex.dto.BranchDto;
import com.backoffice.fitandflex.entity.Branch;
import com.backoffice.fitandflex.repository.BranchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Servicio para gestión de sucursales
 */
@Service
@RequiredArgsConstructor
@Transactional
public class BranchService {

    private final BranchRepository branchRepository;

    /**
     * Crear una nueva sucursal
     */
    public BranchDto.Response createBranch(BranchDto.CreateRequest request) {
        // Verificar si ya existe una sucursal con el mismo nombre
        if (branchRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Ya existe una sucursal con el nombre: " + request.getName());
        }

        Branch branch = Branch.builder()
                .name(request.getName())
                .address(request.getAddress())
                .city(request.getCity())
                .state(request.getState())
                .country(request.getCountry())
                .phone(request.getPhone())
                .email(request.getEmail())
                .build();

        Branch savedBranch = branchRepository.save(branch);
        return BranchDto.Response.fromEntity(savedBranch);
    }

    /**
     * Obtener todas las sucursales (paginado)
     */
    @Transactional(readOnly = true)
    public Page<BranchDto.Response> getAllBranches(Pageable pageable) {
        return branchRepository.findAll(pageable)
                .map(BranchDto.Response::fromEntity);
    }

    /**
     * Obtener todas las sucursales (lista completa)
     */
    @Transactional(readOnly = true)
    public List<BranchDto.SummaryResponse> getAllBranchesSummary() {
        return branchRepository.findAll().stream()
                .map(BranchDto.SummaryResponse::fromEntity)
                .toList();
    }

    /**
     * Obtener sucursal por ID
     */
    @Transactional(readOnly = true)
    public BranchDto.Response getBranchById(Long id) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Sucursal no encontrada con ID: " + id));

        return BranchDto.Response.fromEntity(branch);
    }

    /**
     * Obtener sucursal por nombre
     */
    @Transactional(readOnly = true)
    public Optional<BranchDto.Response> getBranchByName(String name) {
        return branchRepository.findByName(name)
                .map(BranchDto.Response::fromEntity);
    }

    /**
     * Buscar sucursales por ciudad
     */
    @Transactional(readOnly = true)
    public List<BranchDto.SummaryResponse> getBranchesByCity(String city) {
        return branchRepository.findByCityIgnoreCase(city).stream()
                .map(BranchDto.SummaryResponse::fromEntity)
                .toList();
    }

    /**
     * Actualizar sucursal
     */
    public BranchDto.Response updateBranch(Long id, BranchDto.UpdateRequest request) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Sucursal no encontrada con ID: " + id));

        // Verificar si el nuevo nombre ya existe en otra sucursal
        if (request.getName() != null && !request.getName().equals(branch.getName())) {
            if (branchRepository.existsByName(request.getName())) {
                throw new IllegalArgumentException("Ya existe una sucursal con el nombre: " + request.getName());
            }
        }

        // Actualizar campos si se proporcionan
        if (request.getName() != null) {
            branch.setName(request.getName());
        }
        if (request.getAddress() != null) {
            branch.setAddress(request.getAddress());
        }
        if (request.getCity() != null) {
            branch.setCity(request.getCity());
        }
        if (request.getState() != null) {
            branch.setState(request.getState());
        }
        if (request.getCountry() != null) {
            branch.setCountry(request.getCountry());
        }
        if (request.getPhone() != null) {
            branch.setPhone(request.getPhone());
        }
        if (request.getEmail() != null) {
            branch.setEmail(request.getEmail());
        }

        Branch updatedBranch = branchRepository.save(branch);
        return BranchDto.Response.fromEntity(updatedBranch);
    }

    /**
     * Eliminar sucursal
     */
    public void deleteBranch(Long id) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Sucursal no encontrada con ID: " + id));

        // Verificar si la sucursal tiene usuarios asociados
        if (!branch.getUsers().isEmpty()) {
            throw new IllegalStateException("No se puede eliminar la sucursal porque tiene usuarios asociados");
        }

        branchRepository.delete(branch);
    }

    /**
     * Verificar si existe una sucursal por nombre
     */
    @Transactional(readOnly = true)
    public boolean existsByName(String name) {
        return branchRepository.existsByName(name);
    }

    /**
     * Contar total de sucursales
     */
    @Transactional(readOnly = true)
    public long countBranches() {
        return branchRepository.count();
    }
}