/**
 * The decoded jwt token of a successfully authenticated user.
 */
export interface JWTToken {
  userId: string;
  fullName: string;
  userRole: string; // STUDENT, TUTOR, TEACHER
  classroomId: string;
  exp: number;
}
