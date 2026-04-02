package com.dtoan.project.fptassetmanagement.api;

import com.dtoan.project.fptassetmanagement.api.dto.ApiDtos;
import com.dtoan.project.fptassetmanagement.entity.Asset;
import com.dtoan.project.fptassetmanagement.entity.AssetUsage;
import com.dtoan.project.fptassetmanagement.entity.Room;
import com.dtoan.project.fptassetmanagement.entity.User;
import com.dtoan.project.fptassetmanagement.enums.UsageStatus;
import com.dtoan.project.fptassetmanagement.repository.RoomRepository;
import com.dtoan.project.fptassetmanagement.service.AssetService;
import com.dtoan.project.fptassetmanagement.service.impl.AssetUsageService;
import com.dtoan.project.fptassetmanagement.service.impl.CurrentUserService;
import com.dtoan.project.fptassetmanagement.service.impl.NotificationService;
import com.dtoan.project.fptassetmanagement.service.impl.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/usages")
@RequiredArgsConstructor
public class UsageApiController {

    private final AssetUsageService assetUsageService;
    private final AssetService assetService;
    private final RoomRepository roomRepository;
    private final RoomService roomService;
    private final CurrentUserService currentUserService;
    private final NotificationService notificationService;
    private final ApiMapper apiMapper;

    @GetMapping
    public ApiDtos.PageDto<ApiDtos.UsageDto> list(@RequestParam(required = false) String keyword,
                                                  @RequestParam(required = false) UsageStatus status,
                                                  @RequestParam(defaultValue = "0") int page) {
        return apiMapper.toPageDto(
                assetUsageService.searchUsages(keyword, status, PageRequest.of(page, 15)),
                apiMapper::toUsageDto
        );
    }

    @PostMapping("/checkin")
    public ApiDtos.UsageDto checkIn(@RequestBody ApiDtos.CheckInRequest request, Authentication authentication) {
        User user = currentUserService.requireUser(authentication);
        Asset asset = assetService.findByQaCode(request.qaCode())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thiết bị."));
        if (roomService.isStoreRoomId(request.roomToId())) {
            throw new IllegalArgumentException("Không thể chuyển thiết bị vào kho bằng thao tác này.");
        }
        Room room = request.roomToId() != null
                ? roomRepository.findById(request.roomToId()).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phòng."))
                : null;
        AssetUsage usage = assetUsageService.checkIn(asset, user, room, request.purpose());
        notificationService.pushCheckInNotification(usage, user);
        return apiMapper.toUsageDto(usage);
    }

    @PostMapping("/{id}/checkout")
    public ApiDtos.UsageDto checkOut(@PathVariable Long id,
                                     @RequestBody(required = false) ApiDtos.CheckOutRequest request,
                                     Authentication authentication) {
        User user = currentUserService.requireUser(authentication);
        AssetUsage usage = assetUsageService.checkOut(id, request != null ? request.note() : null);
        notificationService.pushCheckOutNotification(usage, user);
        return apiMapper.toUsageDto(usage);
    }
}
