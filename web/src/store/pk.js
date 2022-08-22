//专门存储websocket发过来信息
export default {
  state: {
    status:"matching",
    //用于显示哪个组件
    //matching匹配界面，playing对战界面
    socket: null,//用户的url，
    opponent_username:"",
    opponent_photo:"",
    gamemap: null,
    a_id: 0,
    a_sx: 0,
    a_sy: 0,
    b_id: 0,
    b_sx: 0,
    b_sy: 0,
    gameObject: null,
    loser: "none", //none, all, A, B
  },
  getters: {
  },
  mutations: {
    updateSocket(state, socket) {
        state.socket = socket;
    },
    updateOpponent(state, opponent) {
        state.opponent_username = opponent.username;
        state.opponent_photo = opponent.photo;
    },
    updateStatus(state, status) {
        state.status = status;
    },
    updateGamemap(state, game) {
      state.gamemap = game.map;
      state.a_id = game.a_id;
      state.a_sx = game.a_sx;
      state.a_sy = game.a_sy;
      state.b_id = game.b_id;
      state.b_sx = game.b_sx;
      state.b_sy = game.b_sy
    },
    updateGameObject(state, gameObject) {//将蛇的信息即gameObject存储到全局中
      state.gameObject = gameObject;
    },
    updateLoser(state, loser) {//存储输的人
      state.loser = loser;
    },

  },
  actions: {
  },
  modules: {
  }
}
