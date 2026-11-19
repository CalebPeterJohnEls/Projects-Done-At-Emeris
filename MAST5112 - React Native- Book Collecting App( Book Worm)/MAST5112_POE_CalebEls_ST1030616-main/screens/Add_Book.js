import React, { useState } from 'react';
import { View, Text, TextInput, TouchableOpacity, SafeAreaView, StyleSheet, Dimensions, Image } from 'react-native';
import { useNavigation } from '@react-navigation/native';
import { Picker } from '@react-native-picker/picker';
import { Header } from 'react-native-elements';

const { width, height } = Dimensions.get('window');

export default function AddBook() {
  const navigation = useNavigation();
  const [title, setTitle] = useState('');
  const [author, setAuthor] = useState('');
  const [genre, setGenre] = useState('');
  const [pages, setPages] = useState('');

  const saveBookData = () => {
    const book = {
      title,
      author,
      genre,
      pages,
    };

    // receiving book data to the Home screen/App.js from Add Book screen /Add_Book.js
    navigation.navigate('Home', { book });



  };

  return (
    <SafeAreaView style={styles.container}>
      <View style={styles.background}></View>
      <Header
        containerStyle={{ marginTop: -200 }}
        backgroundColor='#21c46b'
        leftComponent={
          <View style={{ flexDirection: "row", alignItems: 'center' }}>
            <Image
              source={require('./assets/LOGO.png')}
              style={styles.Logo}
            />
            <Text style={styles.title}>ADD BOOK</Text>
          </View>
        }
      />

      <Text></Text>
      <Text style={styles.text}>TITLE:</Text>
      <TextInput
        value={title}
        style={styles.bookTextInput}
        placeholder="Title"
        onChangeText={text => setTitle(text)}
      />

      <Text style={styles.text}>AUTHOR:</Text>
      <TextInput
        value={author}
        style={styles.bookTextInput}
        placeholder="Author"
        onChangeText={text => setAuthor(text)}
      />

      <Text style={styles.text}>GENRE:</Text>
      <Picker
        style={styles.bookTextInput}
        selectedValue={genre}
        onValueChange={itemValue => setGenre(itemValue)}
      >
        <Picker.Item label="Select Genre" value="" />
        <Picker.Item label="FICTION" value="FICTION" />
        <Picker.Item label="NON-FICTION" value="NON-FICTION" />
        <Picker.Item label="ROMANCE" value="ROMANCE" />
        <Picker.Item label="ACTION" value="ACTION" />
        <Picker.Item label="COMEDY" value="COMEDY" />
      </Picker>

      <Text style={styles.text}>PAGES:</Text>
      <TextInput
        value={pages}
        style={styles.bookTextInput}
        placeholder="Pages"
        onChangeText={text => setPages(text)}
      />

      <Text></Text>
      <TouchableOpacity onPress={saveBookData}>
        <View style={styles.button}>
          <Text style={styles.btnText}>SAVE</Text>
        </View>
        <Text></Text>
        <Text></Text>
        <Text></Text>
      </TouchableOpacity>

      <View style={{ flexDirection: "row", alignItems: 'center' }}>
        <TouchableOpacity onPress={() => navigation.navigate('Home')}>
          <View style={styles.button}>
            <Text style={styles.btnText}>HOME</Text>
          </View>
        </TouchableOpacity>

        <TouchableOpacity onPress={() => navigation.navigate('History')}>
          <View style={styles.button}>
            <Text style={styles.btnText}>HISTORY</Text>
          </View>
        </TouchableOpacity>

        <TouchableOpacity onPress={() => navigation.navigate('Genres')}>
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
    color: '#5283B0',
    marginBottom: 24,
    textAlign: 'center',
  },
  btnText: {
    fontSize: 20,
    color: '#217bc4',
    fontWeight: '800',
  },
  button: {
    borderColor: '1E78CB',
    padding: 10,
    backgroundColor: '#21c46b',
    alignItems: 'center',
    borderRadius: 30,
    height: 60,
    width: 150,
  },
  text: {
    fontSize: 25,
    color: '#6AEC8E',
    textAlign: 'center',
  },
  bookTextInput: {
    backgroundColor: 'transparent',
    borderColor: '#6AEC8E',
    borderWidth: 2,
    color: '#21c46b',
    fontSize: 20,
    width: 300,
    padding: 10,
    marginBottom: 10,
    alignSelf: 'center',
  },
  Logo: {
    width: 200,
    height: 210,
    marginRight: 10,
  },
});




//IIE, 2023. 


//References: 

//IIE, 2023. MOBILE APP SCRIPTING MAST5112 [Module Manual]. The Independent Institute of Education: Unpublished 

//Figma.2023. [Online] Available at: https://www.figma.com/. Accessed [07/09/2023]  

//Canva 2023.[Online] Available at: https://www.canva.com/. Accessed [07/08/2023] 

//Fandom.2023.[Online] Available at: https://adventuretimesuperfans.fandom.com/wiki/Shelby . Accessed [07/08/2023] 

//Stack Overflow. 2023. [Online] Available at:   https://stackoverflow.com/questions/55382042/how-to-show-amount-of-times-user-has-logged-in-using-asyncstorage. Accessed [24/11/2023]

//Bobbyhadz.com. 2023. How to use the forEach() method in React, 2023 [Online] Available at:   https://bobbyhadz.com/blog/react-foreach. Accessed [24/11/2023]

//npmjs. 2021. react-native-table-component, 2021 [Online] Available at:   https://www.npmjs.com/package/react-native-table-component. Accessed [22/11/2023]

//Geeks for Geeks. 2023. How to Create A Simple Counter App using React Native ?, 2023 [Online] Available at:   https://www.geeksforgeeks.org/how-to-create-a-simple-counter-app-using-react-native/. Accessed [24/11/2023]