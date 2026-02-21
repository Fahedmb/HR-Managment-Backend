package com.react.project.Service;

import com.react.project.DTO.MeetingDTO;

import java.util.List;

public interface MeetingService {
    List<MeetingDTO> findAll();
    MeetingDTO findById(Long id);
    List<MeetingDTO> findForUser(Long userId);
    List<MeetingDTO> findOrganizedBy(Long organizerId);
    MeetingDTO create(MeetingDTO dto);
    MeetingDTO update(Long id, MeetingDTO dto);
    MeetingDTO cancel(Long id);
    void delete(Long id);
}
