package com.example.software.financeapp.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "family_relationships")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FamilyRelationship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 父/监护人ID
    @Column(nullable = false)
    private Long parentId;

    // 子/被监护人ID
    @Column(nullable = false)
    private Long childId;

    // 关系类型（父子、母子等）
    @Column(length = 50)
    private String relationshipType;

    // 权限级别（查看、管理、限制等）
    @Column(length = 50)
    private String permissionLevel;

    // 创建时间
    @Column(nullable = false)
    private LocalDateTime createdAt;

    // 关系状态（活跃、已解除等）
    @Column(length = 20)
    private String status;
}