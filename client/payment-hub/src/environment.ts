export const API_PREFIX : string = 'api/v1';

export const AUTH_PATH: string = 'http://localhost:8081/' + API_PREFIX + '/auth';

export const BILL_PATH: string = 'http://localhost:8082/' + API_PREFIX + '/bills';

export const PAYMENT_PATH: string = 'http://localhost:8083/' + API_PREFIX + '/payments';

export const TOAST_CONFIGURATION = {
    timeOut: 2500,
    positionClass: 'toast-top-full-width',
    closeButton: true,
};