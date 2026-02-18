package org.example;

//TIP Чтобы <b>запустить</b> код, нажмите <shortcut actionId="Run"/> или
// кликните по значку <icon src="AllIcons.Actions.Execute"/> в поле редактора.
public class Main {
    public static void main(String[] args) {
        //TIP Нажмите <shortcut actionId="ShowIntentionActions"/> с курсором на подсвеченном тексте,
        // чтобы увидеть, как IntelliJ IDEA предлагает это исправить.
        System.out.printf("Здравствуйте и добро пожаловать!");

        for (int i = 1; i <= 5; i++) {
            //TIP Нажмите <shortcut actionId="Debug"/>, чтобы начать отладку кода. Мы поставили один
            // брейкпоинт <icon src="AllIcons.Debugger.Db_set_breakpoint"/>, но вы можете добавить еще,
            // нажав <shortcut actionId="ToggleLineBreakpoint"/>.
            System.out.println("i = " + i);
        }
    }
}
