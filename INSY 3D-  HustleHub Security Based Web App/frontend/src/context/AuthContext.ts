import { createContext, useContext } from "react";
import type {
  User,
  UserRole,
  LoginCredentials,
  RegisterCredentials,
} from "../models/user.ts";

export interface AuthContextType {
  // variables
  //IIE, 2026
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
  isLoading: boolean; // true while the saved session is being restored on page load
  error: string | null; // last friendly auth error, safe to display
  fieldErrors: Record<string, string>; // per-field server validation messages
  twoFactorPending: boolean; // true after the password step, while the 2FA code is still needed (OWASP, 2026 e)
  // methods
  login: (credentials: LoginCredentials) => Promise<void>;
  register: (credentials: RegisterCredentials) => Promise<void>;
  completeTwoFactor: (code: string) => Promise<void>;
  cancelTwoFactor: () => void;
  logout: () => Promise<void>;
  clearError: () => void;
  hasRole: (...roles: UserRole[]) => boolean;
}
 //IIE, 2026
export const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used inside AuthProvider");
  }
  return context;
};
/* Reference List
The Independent Institute of Education (IIE), 2026. Information Systems 3D [INSY7314/W Module Manual]. The Independent Institute of Education: Unpublished.

OWASP, 2026 e. Multifactor Authentication Cheat Sheet. [online] Available at: <https://cheatsheetseries.owasp.org/cheatsheets/Multifactor_Authentication_Cheat_Sheet.html> [Accessed 23 October 2026].*/