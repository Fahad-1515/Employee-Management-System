import { HttpInterceptorFn } from '@angular/common/http';
import { finalize } from 'rxjs/operators';

export const loadingInterceptor: HttpInterceptorFn = (req, next) => {
  // Simple loading interceptor - you can enhance this later
  return next(req).pipe(
    finalize(() => {
      // Add loading logic here if needed
    })
  );
};
