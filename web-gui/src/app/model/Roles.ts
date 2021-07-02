export const Roles = {
  TEACHER: 'TEACHER',
  TUTOR: 'TUTOR',
  STUDENT: 'STUDENT',
  isDocent: (courseRole: string): boolean => {
    return courseRole === Roles.TEACHER;
  },
  isTutor: (courseRole: string): boolean => {
    return courseRole === Roles.TUTOR;
  }
};
