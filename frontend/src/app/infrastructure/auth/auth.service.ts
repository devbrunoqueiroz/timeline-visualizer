import { Injectable, signal, computed } from '@angular/core';
import { AuthResponseDto } from '../api/dto/auth.dto';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly TOKEN_KEY = 'chronicle_token';

  private readonly _token = signal<string | null>(this.loadToken());

  readonly token = this._token.asReadonly();
  readonly isAuthenticated = computed(() => this._token() !== null);
  readonly currentUserId = computed(() => this.extractClaim('userId'));
  readonly currentEmail = computed(() => this.extractClaim('sub'));

  setSession(response: AuthResponseDto): void {
    localStorage.setItem(this.TOKEN_KEY, response.token);
    this._token.set(response.token);
  }

  logout(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    this._token.set(null);
  }

  private loadToken(): string | null {
    try {
      return localStorage.getItem(this.TOKEN_KEY);
    } catch {
      return null;
    }
  }

  private extractClaim(claim: string): string | null {
    const token = this._token();
    if (!token) return null;
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload[claim] ?? null;
    } catch {
      return null;
    }
  }
}
