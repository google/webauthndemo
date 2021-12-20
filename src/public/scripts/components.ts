import '@material/mwc-top-app-bar-fixed';
import '@material/mwc-button';
import '@material/mwc-checkbox';
import '@material/mwc-dialog';
import '@material/mwc-drawer';
import '@material/mwc-linear-progress';
import '@material/mwc-list';
import '@material/mwc-list/mwc-check-list-item';
import '@material/mwc-radio';
import '@material/mwc-snackbar';
import '@material/mwc-select';
import '@material/mwc-switch';
import '@material/mwc-formfield';
import '@material/mwc-icon-button';
import '@material/mwc-textfield';
import { TopAppBarFixed } from '@material/mwc-top-app-bar-fixed';
import { Drawer } from '@material/mwc-drawer';

const topAppBar = document.querySelector('#top-app-bar') as TopAppBarFixed;
const drawer = document.querySelector('#drawer') as Drawer;

if (topAppBar && drawer) {
  topAppBar.addEventListener('MDCTopAppBar:nav', () => {
    drawer.open = !drawer.open;
  });
}
