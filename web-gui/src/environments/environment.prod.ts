export const environment = {
  production: true,
  wsUrl: window.origin.replace(/^http(s)?/, 'ws$1') + '/rsocket',
};
