package rw.auca.radinfotracker.utilities;

import com.github.javafaker.Faker;
import org.springframework.test.web.servlet.MvcResult;
import rw.auca.radinfotracker.model.*;
import rw.auca.radinfotracker.model.dtos.NewImageTypeDTO;
import rw.auca.radinfotracker.model.dtos.NewInsuranceDTO;
import rw.auca.radinfotracker.model.enums.*;
import rw.auca.radinfotracker.security.dtos.LoginRequest;

import java.time.LocalDate;
import java.util.UUID;

public class Data {
    private static final Faker faker = new Faker();

    public static PatientAppointment createPatientAppointment(){
        Patient patient = createPatient();

        Insurance insurance = createInsurance();

        ImageType imageType = createImageType();

        UserAccount radiologist = createRadiologist();

        UserAccount technician = createTechnician();

        return new PatientAppointment(UUID.randomUUID(), faker.code().asin(), LocalDate.now(), EAppointmentStatus.PENDING, patient, insurance, imageType, radiologist, technician, imageType.getTotalCost() * insurance.getRate());
    }

    public static PatientAppointmentImage createPatientAppointmentImage(){
        PatientAppointment appointment = createPatientAppointment();
        File file = createFile();

        return new PatientAppointmentImage(UUID.randomUUID(), file,faker.name().fullName(), appointment);
    }

    public static PatientAppointmentAudit createPatientAppointmentAudit(){
        PatientAppointment appointment = createPatientAppointment();
        File file = createFile();

        return new PatientAppointmentAudit(appointment, EAuditType.CREATE, UUID.randomUUID(), faker.name().fullName(), faker.internet().emailAddress(), "Testing remarks", file);
    }

    public static File createFile(){
        return new File(UUID.randomUUID(), "Testing image", "", "", 0, EFileSizeType.B, "", EFileStatus.SAVED);
    }

    public static Patient createPatient(){
        return new Patient(UUID.randomUUID(), faker.code().asin(), faker.name().firstName(), faker.name().lastName(), faker.phoneNumber().phoneNumber(), LocalDate.now(), EPatientStatus.ACTIVE, faker.address().streetAddress());
    }

    public static Insurance createInsurance(){
        return new Insurance(UUID.randomUUID(), faker.company().name(), faker.number().randomDouble(2, 0,1), EInsuranceStatus.ACTIVE);
    }

    public static ImageType createImageType(){
        return new ImageType(UUID.randomUUID(), faker.medical().medicineName(), EImageTypeStatus.ACTIVE, Double.valueOf(faker.commerce().price()));
    }

    public static UserAccount createRadiologist(){
        return new UserAccount(UUID.randomUUID(), faker.name().firstName(), faker.name().lastName(), faker.internet().emailAddress(), faker.phoneNumber().phoneNumber(), ERole.RADIOLOGIST, EUserStatus.ACTIVE, ELoginStatus.INACTIVE, faker.internet().password());
    }

    public static UserAccount createTechnician(){
        return new UserAccount(UUID.randomUUID(), faker.name().firstName(), faker.name().lastName(), faker.internet().emailAddress(), faker.phoneNumber().phoneNumber(), ERole.TECHNICIAN, EUserStatus.ACTIVE, ELoginStatus.INACTIVE, faker.internet().password());
    }

    public static UserAccount createAdmin() {
        return new UserAccount(UUID.randomUUID(), faker.name().firstName(), faker.name().lastName(), faker.internet().emailAddress(), faker.phoneNumber().phoneNumber(), ERole.ADMIN, EUserStatus.ACTIVE, ELoginStatus.INACTIVE, faker.internet().password());
    }
    public static NewImageTypeDTO imageTypeDTO(){
        return new NewImageTypeDTO(faker.company().name(),Double.valueOf(faker.commerce().price()));
    }

    public static NewInsuranceDTO insuranceDTO(){
        return new NewInsuranceDTO(faker.company().name(),faker.number().randomDouble(2, 0,1));
    }
}
