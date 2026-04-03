package com.dtoan.project.fptassetmanagement.config;

import com.dtoan.project.fptassetmanagement.entity.AssetCategory;
import com.dtoan.project.fptassetmanagement.entity.Role;
import com.dtoan.project.fptassetmanagement.entity.TechnicianCoverageRule;
import com.dtoan.project.fptassetmanagement.entity.User;
import com.dtoan.project.fptassetmanagement.repository.AssetCategoryRepository;
import com.dtoan.project.fptassetmanagement.repository.RoleRepository;
import com.dtoan.project.fptassetmanagement.repository.TechnicianCoverageRuleRepository;
import com.dtoan.project.fptassetmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@Order(20)
@RequiredArgsConstructor
public class ReferenceDataInitializer implements CommandLineRunner {

    private final AssetCategoryRepository categoryRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final TechnicianCoverageRuleRepository coverageRuleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        syncCategoryCodes();
        syncMaintenanceSeedUsersAndCoverage();
    }

    private void syncCategoryCodes() {
        Map<String, String> categoryCodes = Map.of(
                "Máy tính", "PC",
                "Máy chiếu", "PJT",
                "Điều hòa", "AIR",
                "Bàn ghế", "FUR",
                "Thiết bị mạng", "NET",
                "Màn hình", "MON",
                "Máy in", "PRN",
                "Thiết bị âm thanh", "AUD"
        );

        for (AssetCategory category : categoryRepository.findAll()) {
            String expectedCode = categoryCodes.get(category.getName());
            if (expectedCode != null && !expectedCode.equals(category.getCode())) {
                category.setCode(expectedCode);
                categoryRepository.save(category);
            }
        }
    }

    private void syncMaintenanceSeedUsersAndCoverage() {
        Role maintenanceRole = roleRepository.findByName("MAINTENANCE")
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .name("MAINTENANCE")
                        .description("Nhan vien bao tri")
                        .build()));

        Map<String, AssetCategory> categoriesByName = new LinkedHashMap<>();
        for (AssetCategory category : categoryRepository.findAll()) {
            categoriesByName.put(category.getName(), category);
        }

        List<SeedTechnician> technicians = List.of(
                new SeedTechnician("tech01", "Kỹ thuật viên 01", "tech01@fptasset.local", List.of("Máy tính", "Máy chiếu")),
                new SeedTechnician("tech02", "Kỹ thuật viên 02", "tech02@fptasset.local", List.of("Điều hòa", "Bàn ghế")),
                new SeedTechnician("tech03", "Kỹ thuật viên 03", "tech03@fptasset.local", List.of("Thiết bị mạng", "Màn hình")),
                new SeedTechnician("tech04", "Kỹ thuật viên 04", "tech04@fptasset.local", List.of("Máy in", "Thiết bị âm thanh")),
                new SeedTechnician("tech05", "Kỹ thuật viên 05", "tech05@fptasset.local", List.of("Máy tính", "Điều hòa"))
        );

        for (SeedTechnician seed : technicians) {
            User user = userRepository.findByUsername(seed.username())
                    .orElseGet(() -> User.builder()
                            .username(seed.username())
                            .password(passwordEncoder.encode("Fpt@123456"))
                            .build());

            user.setFullName(seed.fullName());
            user.setEmail(seed.email());
            user.setRole(maintenanceRole);
            user.setIsActive(true);
            user.setEmailVerified(true);
            userRepository.save(user);

            for (String categoryName : seed.categoryNames()) {
                AssetCategory category = categoriesByName.get(categoryName);
                if (category == null) {
                    continue;
                }
                boolean exists = coverageRuleRepository.existsActiveDuplicate(
                        user.getId(),
                        category.getId(),
                        null,
                        ""
                );
                if (!exists) {
                    coverageRuleRepository.save(TechnicianCoverageRule.builder()
                            .technician(user)
                            .category(category)
                            .issueType(null)
                            .sortOrder(0)
                            .isActive(true)
                            .build());
                }
            }
        }
    }

    private record SeedTechnician(
            String username,
            String fullName,
            String email,
            List<String> categoryNames
    ) {
    }
}
