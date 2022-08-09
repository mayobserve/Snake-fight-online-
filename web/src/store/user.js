import $ from 'jquery'

export default {
  state: {
    id: "",
    username: "",
    photo: "",
    token: "",
    is_login: false,
    pulling_info: true,  // 是否正在从云端拉取信息
  },
  getters: {
  },
  mutations: {
    updateUser(state, user) {
        state.id = user.id,
        state.username = user.username,
        state.photo = user.photo,
        state.is_login = user.is_login
    },
    updateToken(state, token) {
        state.token = token;
    },
    logout(state) {
        state.id = "",
        state.username = "",
        state.photo = "",
        state.token = "",
        state.is_login = false
    },
    updatePullingInfo(state, pulling_info) {
        state.pulling_info = pulling_info;
    }
  },
  actions: {
    login(context, data) {
        //context可理解代表store整个全局，即context~store
        $.ajax({
            url:"http://127.0.0.1:3000/user/account/token/",
            type:"post",
            data:{
                username: data.username,
                password: data.password,
            },
            success(resp) {
                if(resp.error_message === "success") {
                    localStorage.setItem("jwt_token", resp.token);
                    //jwt_token名字任取
                    context.commit("updateToken", resp.token);
                    //commit即调用mutations更新token
                    //可理解为context.commit/mutations.updateToken
                    data.success(resp);//自己定义的回调函数
                } else{
                    data.error(resp);
                }
            },
            error(resp) {
                data.error(resp);
            }
        });
    },
    getinfo(context, data) {
        $.ajax({
            url:"http://127.0.0.1:3000/user/account/info/",
            type:"get",
            headers:{//传递表头去验证token
              Authorization: "Bearer " + context.state.token
            },
            success(resp) {
                if(resp.error_message === "success") {
                    context.commit("updateUser", {
                        ...resp,
                        //将resp解构出来：即赋值成一个数据传过去
                        is_login: true,
                    });
                    data.success(resp);
                } else{
                    data.error(resp);
                }
            },
            error(resp) {
                data.error(resp);
            }
          });
    },
    logout(context) {
        context.commit("logout");
        localStorage.removeItem("jwt_token");
    }
  },
  modules: {
  }
}
