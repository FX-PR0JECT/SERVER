package FXPROJECT.CHECKPASS.domain.common.converter;

import FXPROJECT.CHECKPASS.domain.entity.college.Departments;
import FXPROJECT.CHECKPASS.domain.entity.lectures.Lecture;
import FXPROJECT.CHECKPASS.domain.entity.users.Professor;
import FXPROJECT.CHECKPASS.domain.enums.DepartmentsEnum;
import FXPROJECT.CHECKPASS.domain.repository.college.JpaDepartmentRepository;
import FXPROJECT.CHECKPASS.web.common.utils.LectureCodeUtils;
import FXPROJECT.CHECKPASS.web.form.requestForm.lectures.register.LectureRegisterForm;
import FXPROJECT.CHECKPASS.web.form.requestForm.lectures.register.LectureTimeSource;
import FXPROJECT.CHECKPASS.web.service.users.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class LectureRegisterFormToLectureConverter implements Converter<LectureRegisterForm, Lecture> {

    private final UserService userService;
    private final JpaDepartmentRepository jpaDepartmentRepository;
    private final LectureCodeUtils lectureCodeUtils;

    @Override
    public Lecture convert(LectureRegisterForm form) {

        LectureTimeSource lectureTimeSource = extractionLectureTimeSource(form);

        Optional<Departments> departments = getDepartments(form.getDepartments());

        Lecture lecture = transferFormToLecture(form, departments, lectureTimeSource);

        return lecture;
    }

    private Lecture transferFormToLecture(LectureRegisterForm form, Optional<Departments> departments, LectureTimeSource lectureTimeSource) {
        Lecture lecture = new Lecture().builder()
                .lectureCode(form.getLectureCode())
                .professor((Professor) userService.getUser(form.getProfessorId()))
                .lectureName(form.getLectureName())
                .lectureRoom(form.getLectureRoom())
                .lectureGrade(form.getLectureGrade())
                .lectureKind(form.getLectureKind().getKind())
                .lectureGrades(form.getLectureGrades())
                .lectureFull(form.getLectureFull())
                .lectureCount(form.getLectureCount())
                .dayOrNight(form.getDayOrNight())
                .departments(departments.get())
                .lectureTimeCode(lectureCodeUtils.getLectureCode(lectureTimeSource))
                .division(form.getDivision())
                .yearSemester(form.getYearSemester())
                .build();
        return lecture;
    }

    private static LectureTimeSource extractionLectureTimeSource(LectureRegisterForm form) {
        LectureTimeSource lectureTimeSource = new LectureTimeSource().builder()
                .lectureTimes(form.getLectureTimes())
                .lectureDays(form.getLectureDays())
                .lectureStartTime(form.getLectureStartTime())
                .build();
        return lectureTimeSource;
    }

    private Optional<Departments> getDepartments(DepartmentsEnum departmentName) {
        Optional<Departments> byDepartment = jpaDepartmentRepository.findByDepartment(departmentName.getDepartment());
        return byDepartment;
    }
}
