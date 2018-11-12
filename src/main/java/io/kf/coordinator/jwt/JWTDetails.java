package io.kf.coordinator.jwt;

import lombok.Data;

import java.util.Optional;

@Data
class JWTDetails {

  private Optional<JWTApplication> application;
  private Optional<JWTUser> user;

  JWTDetails() {
    this.application = Optional.empty();
    this.user = Optional.empty();
  }

}
