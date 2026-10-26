package com.football.fieldservice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.football.fieldservice.model.Field;
import com.football.fieldservice.model.FieldType;

/**
 * Tang truy cap du lieu san bong.
 * Spring Data tu sinh cau lenh SQL tu ten phuong thuc.
 */
public interface FieldRepository extends JpaRepository<Field, Long> {

    /** Loc san theo loai: GET /api/fields?type=SAN_7 */
    List<Field> findByTypeOrderByIdAsc(FieldType type);

    List<Field> findAllByOrderByIdAsc();
}
