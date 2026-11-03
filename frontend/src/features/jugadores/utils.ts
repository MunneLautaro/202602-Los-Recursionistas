export function calcularEdad(fechaNacimiento: string | null): number | null {
  if (!fechaNacimiento) {
    return null;
  }

  const match = /^(\d{4})-(\d{2})-(\d{2})$/.exec(fechaNacimiento);
  if (!match) {
    return null;
  }

  const year = Number(match[1]);
  const month = Number(match[2]);
  const day = Number(match[3]);
  const birthDate = new Date(Date.UTC(year, month - 1, day));

  if (
    birthDate.getUTCFullYear() !== year ||
    birthDate.getUTCMonth() !== month - 1 ||
    birthDate.getUTCDate() !== day
  ) {
    return null;
  }

  const today = new Date();
  let age = today.getFullYear() - year;
  const birthdayHasNotOccurred =
    today.getMonth() < month - 1 ||
    (today.getMonth() === month - 1 && today.getDate() < day);

  if (birthdayHasNotOccurred) {
    age -= 1;
  }

  return age >= 0 ? age : null;
}
