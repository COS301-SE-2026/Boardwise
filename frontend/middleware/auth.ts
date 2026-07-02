export default defineNuxtRouteMiddleware((to, from) => {
    // Check for token directly in browser
    const token = localStorage.getItem('access_token');

    if(!token){
        return navigateTo('auth/signin');
    }
});