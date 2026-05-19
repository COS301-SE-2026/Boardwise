import api from './api' // import API

//GET ALL LISTINGS
export const getListings = () => api.get('marketplace/listings');