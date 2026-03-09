import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthResponseDto, LoginDto, RegisterDto } from './dto/auth.dto';

@Injectable({ providedIn: 'root' })
export class AuthApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/auth`;

  register(email: string, password: string): Observable<AuthResponseDto> {
    const body: RegisterDto = { email, password };
    return this.http.post<AuthResponseDto>(`${this.baseUrl}/register`, body);
  }

  login(email: string, password: string): Observable<AuthResponseDto> {
    const body: LoginDto = { email, password };
    return this.http.post<AuthResponseDto>(`${this.baseUrl}/login`, body);
  }
}
