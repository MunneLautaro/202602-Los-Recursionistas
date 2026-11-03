import { fetchClient } from "../../../lib/fetchClient";
import type { LoginUser } from "../types";

export function fetchCurrentUser(): Promise<LoginUser> {
  return fetchClient<LoginUser>("/users/me");
}
