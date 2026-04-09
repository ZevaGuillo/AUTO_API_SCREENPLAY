package com.ticketing.stepdefinitions;

import com.ticketing.config.AuthTokenHolder;
import com.ticketing.config.WaitlistTestData;
import com.ticketing.questions.ResponseStatus;
import com.ticketing.tasks.CancelWaitlist;
import com.ticketing.tasks.GetWaitlistStatus;
import com.ticketing.tasks.JoinWaitlist;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import net.serenitybdd.screenplay.actors.OnStage;
import net.serenitybdd.screenplay.ensure.Ensure;
import net.serenitybdd.screenplay.rest.questions.LastResponse;

public class WaitlistSteps {

    private String activeUserId;
    private String activeToken;

    // ═══════════════════════════════════════════════════════════════════
    // CP_API_WL_JOIN_01 — Join Waitlist Happy Path
    // ═══════════════════════════════════════════════════════════════════

    @Given("el usuario tiene un X-User-Id válido")
    public void elUsuarioTieneUnXUserIdValido() {
        OnStage.theActorCalled("WaitlistUser");
        activeUserId = WaitlistTestData.getTestUserId();
        activeToken = AuthTokenHolder.getUserToken();
    }

    @Given("existe un evento válido con sección {string}")
    public void existeUnEventoValidoConSeccion(String section) {
        // Event + section already created by WaitlistTestHooks @Before
        // Just verify the data is available
        if (WaitlistTestData.getEventId() == null) {
            throw new IllegalStateException("Event was not seeded by hooks");
        }
    }

    @When("envía una solicitud para unirse a la waitlist")
    public void enviaUnaSolicitudParaUnirseALaWaitlist() {
        OnStage.theActorInTheSpotlight().attemptsTo(
                JoinWaitlist.forUser(
                        activeUserId,
                        WaitlistTestData.getEventId(),
                        WaitlistTestData.WAITLIST_EVENT_SECTION,
                        activeToken)
        );
    }

    @Then("el sistema responde con código {int}")
    public void elSistemaRespondeConCodigo(int expectedStatus) {
        OnStage.theActorInTheSpotlight().attemptsTo(
                Ensure.that(ResponseStatus.code()).isEqualTo(expectedStatus)
        );
    }

    @And("retorna la información del registro")
    public void retornaLaInformacionDelRegistro() {
        String body = LastResponse.received()
                .answeredBy(OnStage.theActorInTheSpotlight())
                .body().asString();
        OnStage.theActorInTheSpotlight().attemptsTo(
                Ensure.that(body).isNotEmpty()
        );
    }

    @And("incluye la URL de status en el header Location")
    public void incluyeLaUrlDeStatusEnElHeaderLocation() {
        String location = LastResponse.received()
                .answeredBy(OnStage.theActorInTheSpotlight())
                .header("Location");
        OnStage.theActorInTheSpotlight().attemptsTo(
                Ensure.that(location).isNotNull(),
                Ensure.that(location).contains("status")
        );
    }

    // ═══════════════════════════════════════════════════════════════════
    // CP_API_WL_CANCEL_02 — Cancel Waitlist (usuario no existe)
    // ═══════════════════════════════════════════════════════════════════

    @Given("el usuario no está en la waitlist")
    public void elUsuarioNoEstaEnLaWaitlist() {
        OnStage.theActorCalled("NonExistentUser");
        activeUserId = WaitlistTestData.NON_EXISTENT_USER_ID;
        activeToken = AuthTokenHolder.getAdminToken();
    }

    @When("intenta cancelar")
    public void intentaCancelar() {
        OnStage.theActorInTheSpotlight().attemptsTo(
                CancelWaitlist.forUser(
                        activeUserId,
                        WaitlistTestData.getEventId(),
                        WaitlistTestData.WAITLIST_EVENT_SECTION,
                        activeToken)
        );
    }

    // ═══════════════════════════════════════════════════════════════════
    // CP_API_WL_STATUS_02 — Get Status (usuario no existe)
    // ═══════════════════════════════════════════════════════════════════

    @Given("el usuario no está registrado en la waitlist")
    public void elUsuarioNoEstaRegistradoEnLaWaitlist() {
        OnStage.theActorCalled("UnregisteredUser");
        activeUserId = WaitlistTestData.NON_EXISTENT_USER_ID;
        activeToken = AuthTokenHolder.getAdminToken();
    }

    @When("consulta su estado")
    public void consultaSuEstado() {
        OnStage.theActorInTheSpotlight().attemptsTo(
                GetWaitlistStatus.forUser(
                        activeUserId,
                        WaitlistTestData.getEventId(),
                        WaitlistTestData.WAITLIST_EVENT_SECTION,
                        activeToken)
        );
    }

    @And("retorna mensaje de error")
    public void retornaMensajeDeError() {
        String body = LastResponse.received()
                .answeredBy(OnStage.theActorInTheSpotlight())
                .body().asString();
        OnStage.theActorInTheSpotlight().attemptsTo(
                Ensure.that(body).isNotEmpty()
        );
    }
}
