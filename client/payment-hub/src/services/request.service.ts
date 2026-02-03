import { Injectable } from '@angular/core';
import {HttpClient, HttpContext, HttpContextToken} from "@angular/common/http";
import { LocalStorageHelper } from './local-storage.service';

@Injectable({
  providedIn: 'root'
})
export class RequestService {

  constructor(private http: HttpClient, private localStorageService: LocalStorageHelper) { }

  get authToken() {
    return this.localStorageService.getItem('access_token');
  }

  get userDetails() {
    return this.localStorageService.getItem('user_details');
  }

  mergeContexts(...contexts: {context: HttpContextToken<any>, value: any}[]) {

    const httpContext = new HttpContext();

    contexts.forEach(({context, value}) => {
      httpContext.set(context, value);
    });
    return httpContext;
  }

  /**
   * POST request
   * @param apiPath - API endpoint path
   * @param requestData - Request payload
   * @param config - Optional context configuration
   */
  public post(apiPath: string, requestData: object, config?: any) {

    const context = config ? this.mergeContexts(...config) : new HttpContext();

    return this.http.post(apiPath, requestData, {
      headers: {'Content-Type': 'application/json'},
      context: context
    });

  }

  /**
   * POST request with file upload
   * @param apiPath - API endpoint path
   * @param formData - FormData object containing files
   * @param config - Optional context configuration
   */
  public postFile(apiPath: string, formData: FormData, config?: any) {
    
    const context = config ? this.mergeContexts(...config) : new HttpContext();

    return this.http.post(apiPath, formData, {
      context: context // 👈 Don't set Content-Type here
    });

  }

  /**
   * GET request
   * @param apiPath - API endpoint path
   * @param config - Optional context configuration
   */
  public get(apiPath: string, config?: any) {

    const context = config ? this.mergeContexts(...config) : new HttpContext();

    return this.http.get(apiPath, {
      headers: {'Content-Type': 'application/json'},
      context: context
    });

  }

  /**
   * PUT request
   * @param apiPath - API endpoint path
   * @param requestData - Request payload
   * @param config - Optional context configuration
   */
  public put(apiPath: string, requestData: object, config?: any) {

    const context = config ? this.mergeContexts(...config) : new HttpContext();

    return this.http.put(apiPath, requestData, {
      headers: {'Content-Type': 'application/json'},
      context: context
    });

  }

  /**
   * PUT request with file upload
   * @param apiPath - API endpoint path
   * @param formData - FormData object containing files
   * @param config - Optional context configuration
   */
  public putFile(apiPath: string, formData: FormData, config?: any) {

    const context = config ? this.mergeContexts(...config) : new HttpContext();

    return this.http.put(apiPath, formData, {
      context: context // 👈 Don't set Content-Type here
    });

  }

  /**
   * PATCH request
   * @param apiPath - API endpoint path
   * @param requestData - Request payload
   * @param config - Optional context configuration
   */
  public patch(apiPath: string, requestData: object, config?: any) {

    const context = config ? this.mergeContexts(...config) : new HttpContext();

    return this.http.patch(apiPath, requestData, {
      headers: {'Content-Type': 'application/json'},
      context: context
    });

  }

  /**
   * DELETE request
   * @param apiPath - API endpoint path
   * @param config - Optional context configuration
   */
  public delete(apiPath: string, config?: any) {

    const context = config ? this.mergeContexts(...config) : new HttpContext();

    return this.http.delete(apiPath, {
      headers: {'Content-Type': 'application/json'},
      context: context
    });

  }

  /**
   * DELETE request with payload
   * @param apiPath - API endpoint path
   * @param requestData - Request payload
   * @param config - Optional context configuration
   */
  public deleteWithBody(apiPath: string, requestData: object, config?: any) {

    const context = config ? this.mergeContexts(...config) : new HttpContext();

    return this.http.delete(apiPath, {
      headers: {'Content-Type': 'application/json'},
      body: requestData,
      context: context
    });

  }

}