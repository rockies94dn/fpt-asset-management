package com.dtoan.project.fptassetmanagement.api;

import com.dtoan.project.fptassetmanagement.api.dto.ApiDtos;
import com.dtoan.project.fptassetmanagement.entity.Asset;
import com.dtoan.project.fptassetmanagement.entity.User;
import com.dtoan.project.fptassetmanagement.enums.AssetStatus;
import com.dtoan.project.fptassetmanagement.repository.AssetCategoryRepository;
import com.dtoan.project.fptassetmanagement.repository.MaintenanceRequestRepository;
import com.dtoan.project.fptassetmanagement.repository.RoomRepository;
import com.dtoan.project.fptassetmanagement.service.AssetService;
import com.dtoan.project.fptassetmanagement.service.impl.CurrentUserService;
import com.dtoan.project.fptassetmanagement.service.impl.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;

@RestController
@RequestMapping("/api/assets")
@RequiredArgsConstructor
public class AssetApiController {

    private final AssetService assetService;
    private final AssetCategoryRepository categoryRepository;
    private final RoomRepository roomRepository;
    private final MaintenanceRequestRepository maintenanceRequestRepository;
    private final RoomService roomService;
    private final CurrentUserService currentUserService;
    private final ApiMapper apiMapper;

    @GetMapping
    public ApiDtos.PageDto<ApiDtos.AssetDto> list(@RequestParam(required = false) String keyword,
                                                  @RequestParam(required = false) AssetStatus status,
                                                  @RequestParam(required = false) Long categoryId,
                                                  @RequestParam(required = false) Long roomId,
                                                  @RequestParam(defaultValue = "false") boolean attentionOnly,
                                                  @RequestParam(defaultValue = "0") int page,
                                                  @RequestParam(defaultValue = "12") int size) {
        return apiMapper.toPageDto(
                assetService.searchAssets(
                        keyword,
                        status,
                        categoryId,
                        roomId,
                        attentionOnly,
                        PageRequest.of(page, size, Sort.by("createdAt").descending())
                ),
                apiMapper::toAssetDto
        );
    }

    @GetMapping("/meta")
    public Map<String, Object> meta() {
        return Map.of(
                "categories", categoryRepository.findAllByOrderByNameAsc().stream().map(apiMapper::toCategoryDto).toList(),
                "rooms", roomService.getAssignableRooms().stream().map(apiMapper::toRoomDto).toList(),
                "statuses", Arrays.stream(AssetStatus.values()).map(status -> Map.of(
                        "value", status.name(),
                        "label", status.getDisplayName()
                )).toList()
        );
    }

    @GetMapping("/{id}")
    public ApiDtos.AssetDetailDto detail(@PathVariable Long id) {
        Asset asset = assetService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thiết bị."));
        return apiMapper.toAssetDetailDto(
                asset,
                maintenanceRequestRepository.findByAssetIdOrderByReportedAtDesc(asset.getId())
        );
    }

    @GetMapping("/scan/{qaCode}")
    public ApiDtos.AssetDto scan(@PathVariable String qaCode) {
        Asset asset = assetService.findByQaCode(qaCode)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thiết bị."));
        return apiMapper.toAssetDto(asset);
    }

    @PostMapping
    public ApiDtos.AssetDto create(@RequestBody ApiDtos.AssetUpsertRequest request, Authentication authentication) {
        User actor = currentUserService.requireUser(authentication);
        Asset asset = new Asset();
        applyAssetRequest(asset, request, actor, true);
        return apiMapper.toAssetDto(assetService.save(asset));
    }

    @PutMapping("/{id}")
    public ApiDtos.AssetDto update(@PathVariable Long id,
                                   @RequestBody ApiDtos.AssetUpsertRequest request,
                                   Authentication authentication) {
        User actor = currentUserService.requireUser(authentication);
        Asset asset = assetService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thiết bị."));
        applyAssetRequest(asset, request, actor, false);
        return apiMapper.toAssetDto(assetService.save(asset));
    }

    @DeleteMapping("/{id}")
    public ApiDtos.SimpleMessageResponse delete(@PathVariable Long id, Authentication authentication) {
        User actor = currentUserService.requireUser(authentication);
        if (!actor.isAdmin()) {
            throw new IllegalStateException("Chỉ quản trị viên mới được xóa thiết bị.");
        }
        assetService.deleteById(id);
        return new ApiDtos.SimpleMessageResponse("Đã xóa thiết bị.");
    }

    private void applyAssetRequest(Asset asset, ApiDtos.AssetUpsertRequest request, User actor, boolean createMode) {
        asset.setName(request.name());
        asset.setBrand(request.brand());
        asset.setModel(request.model());
        asset.setSerialNumber(request.serialNumber());
        asset.setPurchaseDate(request.purchaseDate());
        asset.setPurchasePrice(request.purchasePrice());
        asset.setWarrantyExpiry(request.warrantyExpiry());
        asset.setDescription(request.description());

        asset.setCategory(categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy loại thiết bị.")));

        if (createMode) {
            asset.setQaCode(request.autoGenerateCode() || request.qaCode() == null || request.qaCode().isBlank()
                    ? assetService.generateQaCode(asset.getCategory().getName().substring(0, Math.min(3, asset.getCategory().getName().length())))
                    : request.qaCode().trim());
            asset.setCreatedBy(actor);
            asset.setRoom(roomService.getOrCreateStoreRoom());
            asset.setStatus(actor.isAdmin() && request.status() != null
                    ? AssetStatus.valueOf(request.status())
                    : AssetStatus.AVAILABLE);
            return;
        }

        if (request.status() != null && actor.isAdmin()) {
            asset.setStatus(AssetStatus.valueOf(request.status()));
        }
        if (request.roomId() != null && actor.isAdmin()) {
            asset.setRoom(roomRepository.findById(request.roomId())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phòng.")));
        }
    }
}
