import axios from 'axios'

export default {
    getCategories(){
        return axios.get(`/categories/`)
    }


    
}