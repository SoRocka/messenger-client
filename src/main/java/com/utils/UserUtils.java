package com.utils;

import java.util.ArrayList;
import java.util.List;

public class UserUtils {

    /**
     * Фильтрует список пользователей по введенному тексту.
     * 
     * @param users  список всех пользователей
     * @param filter текст для фильтрации
     * @return отфильтрованный список пользователей
     */
    public static List<String> filterUsers(List<String> users, String filter) {
        List<String> filteredUsers = new ArrayList<>();
        filter = filter.toLowerCase();  // Приводим фильтр к нижнему регистру для поиска
        for (String user : users) {
            if (user.toLowerCase().contains(filter)) {
                filteredUsers.add(user);
            }
        }
        return filteredUsers;
    }

    /**
     * Заменяет имя текущего пользователя на "Заметки" в списке пользователей.
     * 
     * @param users   список всех пользователей
     * @param username имя текущего пользователя
     * @return список с замененным именем пользователя
     */
    public static List<String> replaceUserWithNotes(List<String> users, String username) {
        List<String> modifiedUsers = new ArrayList<>(users);
        modifiedUsers.replaceAll(user -> user.equals(username) ? "Заметки" : user);
        return modifiedUsers;
    }
}
