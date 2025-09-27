package com.babymate.clinic.model;

import jakarta.persistence.*;

@Entity
@Table(name = "clinic")
public class Clinic {
  @Id
  @Column(name = "clinic_id")
  private Integer clinicId;

  @Column(name = "clinic_name")
  private String clinicName;

  // getter / setter
  public Integer getClinicId() { return clinicId; }
  public void setClinicId(Integer clinicId) { this.clinicId = clinicId; }
  public String getClinicName() { return clinicName; }
  public void setClinicName(String clinicName) { this.clinicName = clinicName; }
}
