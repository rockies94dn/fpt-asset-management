package com.dtoan.project.fptassetmanagement.service.impl;

import com.dtoan.project.fptassetmanagement.entity.Room;
import com.dtoan.project.fptassetmanagement.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomService {

    public static final String STORE_ROOM_CODE = "STORE";
    public static final String STORE_ROOM_NAME = "Store Room";

    private final RoomRepository roomRepository;

    public Room getRoomById(Long roomId) {
        return roomRepository.findById(roomId).orElse(null);
    }

    public Room getRoomByCode(String code) {
        return roomRepository.findByCode(code);
    }

    @Transactional(readOnly = true)
    public List<Room> getAssignableRooms() {
        return roomRepository.findByIsActiveTrueOrderByCodeAsc()
                .stream()
                .filter(room -> !isStoreRoom(room))
                .toList();
    }

    public boolean isStoreRoom(Room room) {
        return room != null && STORE_ROOM_CODE.equalsIgnoreCase(room.getCode());
    }

    public boolean isStoreRoomId(Long roomId) {
        if (roomId == null) {
            return false;
        }
        return roomRepository.findById(roomId)
                .map(this::isStoreRoom)
                .orElse(false);
    }

    @Transactional
    public Room getOrCreateStoreRoom() {
        Room existingRoom = roomRepository.findByCode(STORE_ROOM_CODE);
        if (existingRoom != null) {
            if (!Boolean.TRUE.equals(existingRoom.getIsActive())) {
                existingRoom.setIsActive(true);
                return roomRepository.save(existingRoom);
            }
            return existingRoom;
        }

        Room storeRoom = Room.builder()
                .code(STORE_ROOM_CODE)
                .name(STORE_ROOM_NAME)
                .description("Kho mac dinh cho thiet bi moi tao va thiet bi da tra.")
                .isActive(true)
                .build();

        return roomRepository.save(storeRoom);
    }
}

