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

//GET USER LISTINGS 
export const getUserListings = ()=> api.get('marketplace/listings/user');

// UPDATE LISTING BY ID
export const updateListing = (id: string, data: any, image?: File) => {
    const formData = new FormData();
    formData.append('data', new Blob([JSON.stringify(data)], { type: 'application/json' }));

    if (image) {
        formData.append('image', image);
    }

    return api.patch(`marketplace/listing/${id}`, formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
    });
}

//DELETE LISTING 
export const deleteListing = (id: string) => 
  api.delete(`marketplace/listing/${id}`)

//GET LISTING BY ID
export const getListingById = (id: string) => 
  api.get(`marketplace/listings/${id}`)
