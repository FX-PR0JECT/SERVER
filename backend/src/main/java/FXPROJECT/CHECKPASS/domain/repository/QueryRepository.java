package FXPROJECT.CHECKPASS.domain.repository;

import FXPROJECT.CHECKPASS.domain.common.exception.NoCourseHistory;

import FXPROJECT.CHECKPASS.domain.entity.beacon.Beacon;
import FXPROJECT.CHECKPASS.domain.entity.lectures.Enrollment;
import FXPROJECT.CHECKPASS.domain.entity.lectures.Lecture;
import FXPROJECT.CHECKPASS.domain.enums.CollegesEnum;
import FXPROJECT.CHECKPASS.domain.enums.DepartmentsEnum;
import FXPROJECT.CHECKPASS.web.common.searchCondition.lectures.LectureSearchCondition;
import FXPROJECT.CHECKPASS.web.common.searchCondition.users.ProfessorSearchCondition;
import FXPROJECT.CHECKPASS.web.common.searchCondition.users.StudentSearchCondition;
import FXPROJECT.CHECKPASS.domain.common.exception.NoSearchResultsFound;
import FXPROJECT.CHECKPASS.domain.entity.users.*;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.*;

import static FXPROJECT.CHECKPASS.domain.entity.attendance.QAttendance.*;
import static FXPROJECT.CHECKPASS.domain.entity.beacon.QBeacon.*;
import static FXPROJECT.CHECKPASS.domain.entity.lectures.QEnrollment.*;
import static FXPROJECT.CHECKPASS.domain.entity.lectures.QLecture.*;
import static FXPROJECT.CHECKPASS.domain.entity.users.QProfessor.*;
import static FXPROJECT.CHECKPASS.domain.entity.users.QStaff.*;
import static FXPROJECT.CHECKPASS.domain.entity.users.QStudents.*;

@Slf4j
@Repository
public class QueryRepository {

    private final EntityManager em;
    private final JPAQueryFactory query;

    @Autowired
    public QueryRepository(EntityManager em) {
        this.em = em;
        this.query = new JPAQueryFactory(em);
    }

    public List<Professor> getProfessorList(ProfessorSearchCondition condition, Pageable pageable) {

        String college = CollegesEnum.valueOf(condition.getCollege()).getCollege();
        String department = DepartmentsEnum.valueOf(condition.getDepartment()).getDepartment();
        log.info("method :{}" , department);

        List<Professor> result = query
                .select(professor)
                .from(professor)
                .where(equalProfessorCollege(college),equalProfessorDepartment(department))
                .orderBy(
                        professor.userId.asc()
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        if (result.isEmpty()){
            throw new NoSearchResultsFound();
        }

        return result;
    }


    public List<Staff> getStaffList(ProfessorSearchCondition condition, Pageable pageable) {

        String college = CollegesEnum.valueOf(condition.getCollege()).getCollege();
        String department = DepartmentsEnum.valueOf(condition.getDepartment()).getDepartment();
        log.info("method :{}" ,department);

        List<Staff> result = query
                .select(staff)
                .from(staff)
                .where(equalStaffCollege(college),equalStaffDepartment(department))
                .orderBy(
                        staff.userId.asc()
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        if (result.isEmpty()){
            throw new NoSearchResultsFound();
        }

        return result;
    }

    public List<Students> getStudentList(StudentSearchCondition condition,Pageable pageable) {

        OrderSpecifier[] orderSpecifiers = createOrderSpecifier();

        String userId = condition.getUserId().substring(0,2);
        String grade = condition.getGrade();
        String dayOrNight = condition.getDayOrNight();
        String semester = condition.getSemester();
        String college = CollegesEnum.valueOf(condition.getCollege()).getCollege();
        String department = DepartmentsEnum.valueOf(condition.getDepartment()).getDepartment();

        log.info("semester : {}" , semester);

        List<Students> result = query
                .select(students)
                .from(students)
                .where(equalStudentCollege(college),equalStudentDepartment(department),
                        likeUserId(userId),eqUserGrade(grade),eqDayOrNight(dayOrNight),eqSemester(semester))
                .orderBy(
                        orderSpecifiers
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        if (result.isEmpty()){
            throw new NoSearchResultsFound();
        }

        return result;
    }

    public List<Lecture> getEnrollmentList(Students student) {

        Long studentId = student.getUserId();
        String semester = student.getStudentSemester();

        List<Lecture> result = query
                .select(enrollment.lecture)
                .from(enrollment)
                .where(checkEnrollment(studentId), checkEnrollmentYearSemester(semester))
                .fetch();

        return result;
    }

    public List<Lecture> getLectureList(LectureSearchCondition condition, String yearSemester){

        List<Integer> gradeList = condition.getGrade();
        List<String> kindList = condition.getKind();
        List<Integer> gradesList = condition.getGrades();
        Long lectureCode = condition.getLectureCode();
        String lectureName = condition.getLectureName();
        String professorName = condition.getProfessorName();

        List<Lecture> result = query
                .select(lecture)
                .from(lecture)
                .where(orLectureGrade(gradeList), orLectureKind(kindList), orLectureGrades(gradesList), checkLectureYearSemester(yearSemester),
                        eqLectureCode(lectureCode), eqLectureName(lectureName), eqProfessorName(professorName))
                .fetch();

        if (result.isEmpty()){
            throw new NoSearchResultsFound();
        }
        return result;
    }

    public List<Lecture> getLectureList(String yearSemester) {
        List<Lecture> result = query
                .select(lecture)
                .from(lecture)
                .where(checkLectureYearSemester(yearSemester))
                .fetch();

        if (result.isEmpty()){
            throw new NoSearchResultsFound();
        }
        return result;
    }

    public List<Lecture> getLectureList(int major, int minor) {

        List<Lecture> result = query
                .select(lecture)
                .from(lecture)
                .where(eqLectureMajor(major), eqLectureMinor(minor))
                .fetch();

        if (result.isEmpty()) {
            throw new NoSearchResultsFound();
        }
        return result;
    }

    public List<String> getYearSemesterList(Students student){

        List<String> result = query
                .select(enrollment.yearSemester).distinct()
                .from(enrollment)
                .where(likeStudentId(student.getUserId()))
                .fetch();

        if (result.isEmpty()){
            throw new NoCourseHistory();
        }

        return result;
    }

    public List<Enrollment> getCourseList(Students student){

        List<Enrollment> result = query
                .select(enrollment)
                .from(enrollment)
                .where(likeStudentId(student.getUserId()))
                .fetch();

        if (result.isEmpty()){
            throw new NoCourseHistory();
        }

        return result;
    }

    public List<Beacon> getBeaconList() {
        List<Beacon> result = query
                .select(beacon)
                .from(beacon)
                .fetch();
        if (result.isEmpty()){
            throw new NoSearchResultsFound();
        }

        return result;
    }

    public void deleteAttendanceWeek(Long userId, Long lectureCode, String studentGrade, String studentSemester){
        String attendanceId = userId.toString() + lectureCode.toString() + studentGrade + studentSemester;
        query.delete(attendance).where(likeAttendanceId(attendanceId)).execute();
    }

    private BooleanExpression checkEnrollment(Long userId) {
        if (userId != null && userId > 0){
            return enrollment.student.userId.eq(userId);
        }
        return null;
    }

    private BooleanExpression eqLectureMajor(int major) {
        if (major > 0){
            return lecture.beacon.beaconPK.major.eq(major);
        }
        return null;
    }

    private BooleanExpression eqLectureMinor(int minor) {
        if (minor > 0){
            return lecture.beacon.beaconPK.minor.eq(minor);
        }
        return null;
    }

    private BooleanExpression checkEnrollmentYearSemester(String semester){
        String yearSemester = LocalDate.now().getYear() + "년도 " + semester;
        return enrollment.yearSemester.eq(yearSemester);
    }

    private BooleanExpression checkLectureYearSemester(String yearSemester){
        return lecture.yearSemester.eq(yearSemester);
    }

    private BooleanExpression orLectureGrade(List<Integer> gradeList) {
        if (gradeList != null){
            return lecture.lectureGrade.in(gradeList);
        }
        return null;
    }

    private BooleanExpression orLectureKind(List<String> kindList) {
        if (kindList != null){
            return lecture.lectureKind.in(kindList);
        }
        return null;
    }

    private BooleanExpression orLectureGrades(List<Integer> gradesList){
        if (gradesList != null) {
            return lecture.lectureGrades.in(gradesList);
        }
        return null;
    }

    private BooleanExpression eqLectureCode(Long lectureCode){
        if (lectureCode != null && lectureCode > 0){
            return lecture.lectureCode.eq(lectureCode);
        }
        return null;
    }

    private BooleanExpression eqLectureName(String lectureName){
        if (StringUtils.hasText(lectureName)){
            return lecture.lectureName.eq(lectureName);
        }
        return null;
    }

    private BooleanExpression eqProfessorName(String professorName) {
        if (StringUtils.hasText(professorName)){
            return lecture.professor.userName.eq(professorName);
        }
        return null;
    }

    private BooleanExpression equalProfessorCollege(String userCollege){
        if (StringUtils.hasText(userCollege)){
            return professor.departments.colleges.college.eq(userCollege);
        }
        return null;
    }

    private BooleanExpression equalProfessorDepartment(String department){
        if (StringUtils.hasText(department)){
            return professor.departments.department.eq(department);
        }
        return null;
    }

    private BooleanExpression equalStaffCollege(String userCollege){
        if (StringUtils.hasText(userCollege)){
            return staff.departments.colleges.college.eq(userCollege);
        }
        return null;
    }

    private BooleanExpression equalStaffDepartment(String department){
        if (StringUtils.hasText(department)){
            return staff.departments.department.eq(department);
        }
        return null;
    }

    private BooleanExpression equalStudentCollege(String userCollege){
        if (StringUtils.hasText(userCollege)){
            return students.departments.colleges.college.eq(userCollege);
        }
        return null;
    }

    private BooleanExpression equalStudentDepartment(String department){
        if (StringUtils.hasText(department)){
            return students.departments.department.eq(department);
        }
        return null;
    }

    private BooleanExpression likeUserId(String userId){
        if (StringUtils.hasText(userId)){
            return students.userId.like(userId + "%");
        }
        return null;
    }

    private BooleanExpression likeStudentId(Long studentId){
        if (studentId != null && studentId > 0) {
            return enrollment.enrollmentId.like(studentId + "%");
        }
        return null;
    }

    private BooleanExpression likeAttendanceId(String attendanceId) {
        if (StringUtils.hasText(attendanceId)) {
            return attendance.AttendanceId.like(attendanceId + "%");
        }
        return null;
    }

    private BooleanExpression eqUserGrade(String grade){
        if (StringUtils.hasText(grade)){
            return students.studentGrade.eq(grade);
        }
        return null;
    }

    private BooleanExpression eqDayOrNight(String dayOrNight){
        if (StringUtils.hasText(dayOrNight)){
            return students.dayOrNight.eq(dayOrNight);
        }
        return null;
    }

    private BooleanExpression eqSemester(String semester){
        if (StringUtils.hasText(semester)){
            return students.studentSemester.eq(semester);
        }
        return null;
    }


    private OrderSpecifier[] createOrderSpecifier() {
        List<OrderSpecifier> orderSpecifiers = new ArrayList<>();

        orderSpecifiers.add(new OrderSpecifier(Order.ASC, students.userId));
        orderSpecifiers.add(new OrderSpecifier(Order.ASC, students.userName));
        orderSpecifiers.add(new OrderSpecifier(Order.DESC, students.studentGrade));
        orderSpecifiers.add(new OrderSpecifier(Order.ASC, students.studentSemester));
        orderSpecifiers.add(new OrderSpecifier(Order.ASC, students.dayOrNight));

        return orderSpecifiers.toArray(new OrderSpecifier[orderSpecifiers.size()]);
    }
}
