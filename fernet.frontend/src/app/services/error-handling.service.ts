import { Injectable } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';

export interface ApiError {
  message: string;
  statusCode?: number;
  errors?: string[];
}

@Injectable({
  providedIn: 'root'
})
export class ErrorHandlingService {

  handleError(error: HttpErrorResponse): Observable<never> {
    let errorMessage = 'Došlo je do greške. Pokušajte ponovo.';

    if (error.error instanceof ErrorEvent) {
      // Client-side ili network error
      errorMessage = `Greška: ${error.error.message}`;
    } else {
      // Backend error
      switch (error.status) {
        case 400:
          errorMessage = 'Neispravni podaci. Proverite unos.';
          break;
        case 401:
          errorMessage = 'Neispravni kredencijali.';
          break;
        case 403:
          errorMessage = 'Nemate dozvolu za ovu akciju.';
          break;
        case 404:
          errorMessage = 'Traženi resurs nije pronađen.';
          break;
        case 409:
          errorMessage = 'Korisničko ime ili email već postoji.';
          break;
        case 422:
          errorMessage = 'Validacija nije uspešna. Proverite podatke.';
          break;
        case 500:
          errorMessage = 'Greška na serveru. Pokušajte kasnije.';
          break;
        default:
          errorMessage = `Greška ${error.status}: ${error.message}`;
      }

      // Ako server vraća specifičnu poruku
      if (error.error?.message) {
        errorMessage = error.error.message;
      }
    }

    console.error('HTTP Error:', error);
    return throwError(() => new Error(errorMessage));
  }

  getErrorMessage(error: any): string {
    if (error?.error?.message) {
      return error.error.message;
    }

    if (error?.message) {
      return error.message;
    }

    return 'Došlo je do neočekivane greške.';
  }
}
