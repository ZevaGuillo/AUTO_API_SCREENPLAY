@waitlist
Feature: Waitlist API operations

  # ─── CP_API_WL_JOIN_01 ─ Join Waitlist – Happy Path ───────────────────
  Scenario: Usuario se une correctamente a la waitlist
    Given el usuario tiene un X-User-Id válido
    And existe un evento válido con sección "VIP"
    When envía una solicitud para unirse a la waitlist
    Then el sistema responde con código 201
    And retorna la información del registro
    And incluye la URL de status en el header Location

  # ─── CP_API_WL_CANCEL_02 ─ Cancel Waitlist – Usuario no existe ───────
  Scenario: Usuario intenta cancelar sin estar en waitlist
    Given el usuario no está en la waitlist
    When intenta cancelar
    Then el sistema responde con código 404

  # ─── CP_API_WL_STATUS_02 ─ Get Waitlist Status – Usuario no existe ───
  Scenario: Usuario no está en la waitlist
    Given el usuario no está registrado en la waitlist
    When consulta su estado
    Then el sistema responde con código 404
    And retorna mensaje de error
