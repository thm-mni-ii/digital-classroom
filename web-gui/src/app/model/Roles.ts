export const Roles = {
  TEACHER: 'TEACHER',
  TUTOR: 'TUTOR',
  STUDENT: 'STUDENT',
  isTeacher: (courseRole: string): boolean => {
    return courseRole == Roles.TEACHER;
  },
  isTutor: (courseRole: string): boolean => {
    return courseRole == Roles.TUTOR;
  },
  isPrivileged: (courseRole: string): boolean => {
    return Roles.isTeacher(courseRole) || Roles.isTutor(courseRole)
  }
};
