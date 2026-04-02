package com.dtoan.project.fptassetmanagement.api;

import com.dtoan.project.fptassetmanagement.api.dto.ApiDtos;
import com.dtoan.project.fptassetmanagement.entity.Room;
import com.dtoan.project.fptassetmanagement.entity.Role;
import com.dtoan.project.fptassetmanagement.entity.TechnicianCoverageRule;
import com.dtoan.project.fptassetmanagement.entity.User;
import com.dtoan.project.fptassetmanagement.repository.*;
import com.dtoan.project.fptassetmanagement.service.impl.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminApiController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RoomRepository roomRepository;
    private final AssetCategoryRepository categoryRepository;
    private final TechnicianCoverageRuleRepository coverageRuleRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoomService roomService;
    private final ApiMapper apiMapper;

    @GetMapping("/users")
    public List<ApiDtos.UserDto> users() {
        return userRepository.findAll().stream().map(apiMapper::toUserDto).toList();
    }

    @GetMapping("/meta")
    public Map<String, Object> meta() {
        return Map.of(
                "roles", roleRepository.findAll().stream().map(role -> Map.of("id", role.getId(), "name", role.getName())).toList(),
                "rooms", roomRepository.findAll().stream().map(apiMapper::toRoomDto).toList(),
                "categories", categoryRepository.findAllByOrderByNameAsc().stream().map(apiMapper::toCategoryDto).toList(),
                "technicians", userRepository.findByRoleNameAndIsActiveTrueOrderByFullNameAsc("MAINTENANCE").stream().map(apiMapper::toUserDto).toList()
        );
    }

    @PutMapping("/users/{id}")
    public ApiDtos.UserDto updateUser(@PathVariable Long id, @RequestBody ApiDtos.UserUpdateRequest request) {
        User user = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng."));
        Role role = roleRepository.findById(request.roleId()).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy quyền."));

        String fullName = valueOrBlank(request.fullName());
        String username = valueOrBlank(request.username());
        String email = normalizeEmail(request.email());
        String phone = valueOrBlank(request.phone());

        if (fullName.isBlank() || username.isBlank()) {
            throw new IllegalArgumentException("Họ tên và tên đăng nhập không được để trống.");
        }
        if (userRepository.existsByUsernameAndIdNot(username, id)) {
            throw new IllegalArgumentException("Tên đăng nhập đã tồn tại.");
        }
        if (!email.isBlank() && userRepository.existsByEmailAndIdNot(email, id)) {
            throw new IllegalArgumentException("Email đã được sử dụng.");
        }

        user.setFullName(fullName);
        user.setUsername(username);
        user.setEmail(email.isBlank() ? null : email);
        user.setPhone(phone.isBlank() ? null : phone);
        user.setRole(role);
        return apiMapper.toUserDto(userRepository.save(user));
    }

    @PostMapping("/users/{id}/toggle")
    public ApiDtos.UserDto toggleUser(@PathVariable Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng."));
        user.setIsActive(!Boolean.TRUE.equals(user.getIsActive()));
        return apiMapper.toUserDto(userRepository.save(user));
    }

    @PostMapping("/users/{id}/reset-password")
    public ApiDtos.SimpleMessageResponse resetPassword(@PathVariable Long id,
                                                       @RequestBody ApiDtos.PasswordResetRequest request) {
        User user = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng."));
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        return new ApiDtos.SimpleMessageResponse("Đã đặt lại mật khẩu.");
    }

    @GetMapping("/rooms")
    public List<ApiDtos.RoomDto> rooms() {
        return roomRepository.findAll().stream().map(apiMapper::toRoomDto).toList();
    }

    @PostMapping("/rooms")
    public ApiDtos.RoomDto createRoom(@RequestBody ApiDtos.RoomUpsertRequest request) {
        if (roomRepository.existsByCode(request.code())) {
            throw new IllegalArgumentException("Mã phòng đã tồn tại.");
        }
        Room room = Room.builder()
                .code(request.code())
                .name(request.name())
                .building(request.building())
                .floor(request.floor())
                .capacity(request.capacity())
                .description(request.description())
                .isActive(true)
                .build();
        return apiMapper.toRoomDto(roomRepository.save(room));
    }

    @DeleteMapping("/rooms/{id}")
    public ApiDtos.SimpleMessageResponse deleteRoom(@PathVariable Long id) {
        Room room = roomRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phòng."));
        if (roomService.isStoreRoom(room)) {
            throw new IllegalArgumentException("Không thể xóa kho hệ thống.");
        }
        room.setIsActive(false);
        roomRepository.save(room);
        return new ApiDtos.SimpleMessageResponse("Đã xóa phòng.");
    }

    @GetMapping("/coverage-rules")
    public List<ApiDtos.CoverageRuleDto> coverageRules() {
        return coverageRuleRepository.findByIsActiveTrueOrderBySortOrderAscIdAsc().stream()
                .map(apiMapper::toCoverageRuleDto)
                .toList();
    }

    @PostMapping("/coverage-rules")
    public ApiDtos.CoverageRuleDto createCoverageRule(@RequestBody ApiDtos.CoverageRuleSaveRequest request) {
        TechnicianCoverageRule rule = TechnicianCoverageRule.builder()
                .technician(userRepository.findById(request.technicianId())
                        .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy kỹ thuật viên.")))
                .room(request.roomId() != null ? roomRepository.findById(request.roomId())
                        .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phòng.")) : null)
                .category(request.categoryId() != null ? categoryRepository.findById(request.categoryId())
                        .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy loại thiết bị.")) : null)
                .issueType(request.issueType())
                .sortOrder(request.sortOrder() == null ? 0 : request.sortOrder())
                .isActive(request.active() == null || request.active())
                .build();
        return apiMapper.toCoverageRuleDto(coverageRuleRepository.save(rule));
    }

    @DeleteMapping("/coverage-rules/{id}")
    public ApiDtos.SimpleMessageResponse deleteCoverageRule(@PathVariable Long id) {
        coverageRuleRepository.deleteById(id);
        return new ApiDtos.SimpleMessageResponse("Đã xóa quy tắc phân công.");
    }

    private String valueOrBlank(String value) {
        return value == null ? "" : value.trim();
    }

    private String normalizeEmail(String value) {
        return valueOrBlank(value).toLowerCase();
    }
}
