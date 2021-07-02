/**
 * The decoded jwt token of a successfully authenticated user.
 */
export interface JWTToken {
  id: string;
  fullName: string;
  role: string; // STUDENT, TUTOR, TEACHER
  classroomId: string;
  exp: number;
}
