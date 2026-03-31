package com.dtoan.project.fptassetmanagement.config;

import com.dtoan.project.fptassetmanagement.entity.Role;
import com.dtoan.project.fptassetmanagement.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RoleDataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) {
        ensureRoleExists("ADMIN", "Quan tri he thong");
        ensureRoleExists("STAFF", "Nhan vien");
        ensureRoleExists("MAINTENANCE", "Nhan vien bao tri");
    }

    private void ensureRoleExists(String name, String description) {
        if (roleRepository.findByName(name).isPresent()) {
            return;
        }

        roleRepository.save(Role.builder()
                .name(name)
                .description(description)
                .build());
    }
}
