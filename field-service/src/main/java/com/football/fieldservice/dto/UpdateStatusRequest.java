package com.football.fieldservice.dto;

import com.football.fieldservice.model.FieldStatus;

import jakarta.validation.constraints.NotNull;

/**
 * Body cua PUT /api/fields/{id}/status - chu san bat / tat bao tri.
 */
public class UpdateStatusRequest {

    @NotNull(message = "Trạng thái không được để trống, chỉ nhận ACTIVE hoặc MAINTENANCE")
    private FieldStatus status;

    public FieldStatus getStatus() {
        return status;
    }

    public void setStatus(FieldStatus status) {
        this.status = status;
    }
}
