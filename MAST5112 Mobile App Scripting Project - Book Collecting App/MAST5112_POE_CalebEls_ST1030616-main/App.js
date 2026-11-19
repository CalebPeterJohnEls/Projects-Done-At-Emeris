import React, { useState, useEffect } from 'react';
import { View, Text, TouchableOpacity, Dimensions, SafeAreaView, StyleSheet, Image } from 'react-native';
import { useNavigation, useRoute, NavigationContainer } from '@react-navigation/native';
import { createStackNavigator } from '@react-navigation/stack';
import { Header } from 'react-native-elements';

import Add_Book from './screens/Add_Book';
import Genre from './screens/Genre';
import History from './screens/History';

const { width, height } = Dimensions.get('window');

const Stack = createStackNavigator();

export default function App() {
  return (
    <NavigationContainer>
      <Stack.Navigator>
        <Stack.Screen name="Home" component={HomeScreen} />
        <Stack.Screen name="AddBook" component={Add_Book} />
        <Stack.Screen name="Genres" component={Genre} />
        <Stack.Screen name="History" component={History} />
      </Stack.Navigator>
    </NavigationContainer>
  );
}

function HomeScreen({ navigation }) {
  const route = useRoute();
  const { book } = route.params || {};

  const [books, setBooks] = useState([]);

  useEffect(() => {
    if (book) {
      setBooks((prevBooks) => [book, ...prevBooks]);
    }
  }, [book]);

  const lastThreeBooks = books.slice(0, 3);

  const calculateTotalPages = () => {
    return books.reduce((total, book) => total + parseInt(book.pages, 10), 0);
  };

  const calculateAveragePages = () => {
    if (books.length === 0) return 0;

    const total = calculateTotalPages();

    return total / books.length;
  };

  const lastReadBook = books.length > 0 ? books[0] : null;

  return (
    <SafeAreaView style={styles.container}>
      <View style={[styles.background, styles.overflow]}></View>
      <Header
        containerStyle={{ marginTop: -250 }}
        backgroundColor="#21c46b"
        leftComponent={
          <View style={{ flexDirection: 'row', alignItems: 'center' }}>
            <Image
              source={require('./assets/LOGO.png')}
              style={styles.Logo}
            />
            <Text style={styles.title}>HOME</Text>
          </View>
        }
      />

      <Text></Text>
      <Text></Text>
      <Text style={styles.title2}>LAST BOOK READ</Text>
      <Text style={styles.text}>TITLE: {lastReadBook ? lastReadBook.title : 'N/A'}</Text>
      <Text style={styles.text}>AUTHOR: {lastReadBook ? lastReadBook.author : 'N/A'}</Text>
      <Text style={styles.text}>GENRE: {lastReadBook ? lastReadBook.genre : 'N/A'}</Text>
      <Text style={styles.text}>PAGES: {lastReadBook ? lastReadBook.pages : 'N/A'}</Text>
      <Text></Text>
      <Text></Text>
      <Text style={styles.title2}>PAGES READ</Text>

      <Text style={styles.text}>TOTAL: {calculateTotalPages()}</Text>
      <Text style={styles.text}>AVERAGE: {calculateAveragePages().toFixed(2)}</Text>
      <Text></Text>
      <Text></Text>
      <Text></Text>
      <View style={{ flexDirection: 'row', alignItems: 'center' }}>
        <Text></Text>
        <Text></Text>
        <TouchableOpacity onPress={() => navigation.navigate('AddBook')}>
          <View style={styles.button}>
            <Text style={styles.btnText}>ADD BOOK</Text>
          </View>
        </TouchableOpacity>
        <TouchableOpacity onPress={() => navigation.navigate('History', { books: lastThreeBooks })}>
          <View style={styles.button}>
            <Text style={styles.btnText}>HISTORY</Text>
          </View>
        </TouchableOpacity>
        <TouchableOpacity onPress={() => navigation.navigate('Genres', { books })}>
          <View style={styles.button}>
            <Text style={styles.btnText}>GENRES</Text>
          </View>
        </TouchableOpacity>
      </View>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    justifyContent: 'center',
    alignItems: 'center',
    flex: 1,
    backgroundColor: '#217bc4',
  },
  background: {
    position: 'absolute',
    top: 0,
    left: 0,
    width: width,
    height: height,
  },
  title: {
    fontSize: 36,
    color: '#1E78CB',
    marginBottom: 24,
    textAlign: 'center',
  },
  btnText: {
    fontSize: 20,
    color: '#217bc4',
    fontWeight: '800',
  },
  button: {
    borderColor: '#6aec8e',
    padding: 10,
    backgroundColor: '#21c46b',
    alignItems: 'center',
    borderRadius: 30,
    height: 60,
    width: 150,
  },
  title2: {
    fontSize: 30,
    color: '#6aec8e',
    marginBottom: 24,
    textAlign: 'center',
  },
  text: {
    fontSize: 25,
    color: '#6aec8e',
    textAlign: 'center',
  },
  Logo: {
    width: 200,
    height: 210,
    marginRight: 10,
  },
});



//Stack Overflow, 2023.

//React Native Elements. 2023.
//React Native. 2023.


//IIE, 2023. 


//References: 

//IIE, 2023. MOBILE APP SCRIPTING MAST5112 [Module Manual]. The Independent Institute of Education: Unpublished 

//Figma.2023. [Online] Available at: https://www.figma.com/. Accessed [07/09/2023]  

//Canva 2023.[Online] Available at: https://www.canva.com/. Accessed [07/08/2023] 

//Fandom.2023.[Online] Available at: https://adventuretimesuperfans.fandom.com/wiki/Shelby . Accessed [07/08/2023] 

//React Native Elements. 2023. [Online] Available at:  https://reactnativeelements.com/docs/1.2.0/header. Accessed [24/10/2023]

//Stack Overflow. 2023. [Online] Available at:   https://stackoverflow.com/questions/61944110/i-cant-manage-to-get-total-number-of-pages-for-list. Accessed [25/10/2023]

//React Native. 2023. [Online] Available at:  https://reactnative.dev. Accessed [01/10/2023]

//Free Code Camp. 2023. [Online] Available at:  https://forum.freecodecamp.org/t/how-to-get-total-number-from-a-number-string-using-reduce/335141/3. Accessed [25/10/2023]

//Johannes Konings. 2023. [Online] Available at:  https://johanneskonings.dev/react/2020/10/19/example_react_average_of_items_in_different_arrays/ .Accessed [25/10/2023]

//Stack Overflow. 2023. [Online] Available at:   https://stackoverflow.com/questions/55382042/how-to-show-amount-of-times-user-has-logged-in-using-asyncstorage. Accessed [24/11/2023]

//Bobbyhadz.com. 2023. How to use the forEach() method in React, 2023 [Online] Available at:   https://bobbyhadz.com/blog/react-foreach. Accessed [24/11/2023]

//npmjs. 2021. react-native-table-component, 2021 [Online] Available at:   https://www.npmjs.com/package/react-native-table-component. Accessed [22/11/2023]

//Geeks for Geeks. 2023. How to Create A Simple Counter App using React Native ?, 2023 [Online] Available at:   https://www.geeksforgeeks.org/how-to-create-a-simple-counter-app-using-react-native/. Accessed [24/11/2023]
