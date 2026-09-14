export default defineNuxtPlugin((app) => {
    app.hook('app:mounted', () => {
        const { connect } = useStomp();
        const token = localStorage.getItem("access_token");

        if(token)
            connect();
    });
})