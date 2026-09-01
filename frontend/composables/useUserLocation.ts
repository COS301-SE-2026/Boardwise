    interface NominatimAddress {
        city?: string;
        town?: string;
        village?: string;
        suburb?: string;
        residential?:string;
    }

    interface NominatimResponse {
        address: NominatimAddress;
    }
    
export function useUserLocation() {
    const city = ref<string | null>(null);
    const suburb = ref<string | null>(null);
    const lat = ref<number | null>(null);
    const long = ref<number | null>(null);
    const error =ref<string | null>(null);
    const loading = ref(false);

    async function findUserLocation(){
        loading.value = true;

         try {
            //raw coordinates
            const position = await new Promise<GeolocationPosition>((resolve, reject) => {
                if (!navigator.geolocation) {
                    reject(new Error('Geolocation not supported by this browser'));
                    return;
                }
                navigator.geolocation.getCurrentPosition(resolve, reject, {
                    enableHighAccuracy: true,
                    timeout: 10000,
                    maximumAge: 0,
                });
            });

            lat.value = position.coords.latitude;
            long.value = position.coords.longitude;

            //find
            const res = await fetch(
                `https://nominatim.openstreetmap.org/reverse?format=json&lat=${lat.value}&lon=${long.value}`,
                {
                    headers: {
                        'User-Agent': 'Boardwise (worksonmymachine67@gmail.com)'
                    }
                }
            );
            if (!res.ok) throw new Error('Geocoding request failed');

            const data: NominatimResponse = await res.json();
            const address = data.address;


            city.value = address.town ?? address.village ?? null;
            suburb.value = address.residential ?? null;

        } catch (err) {
            error.value = err instanceof Error ? err.message : String(err);
        } finally {
            loading.value = false;
        }
    } 
    return { city, suburb, lat, long, error, loading, findUserLocation}
}