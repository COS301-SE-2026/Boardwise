import api from './api' // import API

//GET ALL LISTINGS
export const getListings = () => api.get('marketplace/listings');

//CREATE LISTING
export const createListing = (data: any, image : File) => {
    const formData = new FormData();
    formData.append('data', new Blob([JSON.stringify(data)],{type:'application/json'}));
    formData.append('image',image);

    return api.post('marketplace/listings',formData,{
        headers:{'Content-Type': 'multipart/form-data'}
    })
}